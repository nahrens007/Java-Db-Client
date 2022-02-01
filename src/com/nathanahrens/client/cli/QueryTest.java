package com.nathanahrens.client.cli;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nathanahrens.client.Client;
import com.nathanahrens.client.IClient;
import com.nathanahrens.client.Source;
import com.nathanahrens.client.User;
import com.nathanahrens.log.Logger;
import com.nathanahrens.pwsafe.Credential;
import com.nathanahrens.pwsafe.SafeWrapper;

public class QueryTest {
	private String sql;
	private String host;
	private String ldapServer;
	private String ldapContext;
	private String domain;
	private String dataSource;
	private int port;
	private String vaultFile;
	private String vaultPassword;
	private String vaultGroup;
	private String vaultTitle;
	private String title;
	private String outputFile;
	
	private Credential sourceCred;
	private ResultSet rs;
	private Logger logger;

	public QueryTest(File jsonFile, Logger logger) {
		super();
		this.logger = logger;
		// Load parameters
		this.readJsonFile(jsonFile);

		// Run client
		this.setCredential(this.vaultGroup, this.vaultTitle);
		if (this.ldapServer == null && this.domain == null) {
			this.logger.log("Must provide details for either Oracle source or Composite source.");
			System.exit(-1);
		}
		if (this.domain != null) {
			this.driveComposite();
		} else {
			this.driveOracle();
		}

		if (this.outputFile == null) {
			this.evaluateResultSet();
		} else {
			JOptionPane.showMessageDialog(
					null, String.format("%s\n-----------\nHost: %s\n-----------\n" + "File written to %s",
							this.title, this.host, this.outputFile),
					"QueryTest Results", JOptionPane.INFORMATION_MESSAGE);
		}
		// Evaluate results
	}

	private void evaluateResultSet() {
		int colCount;
		try {
			colCount = this.rs.getMetaData().getColumnCount();
			if (colCount != 1) {
				this.logger.log("Invalid query - must return 1 column of type number!");
				return;
			}
			for (int r = 1; this.rs.next(); r++) {

				/*-
				 * If you wanted to loop through columns 
				 for (int i = 0; i < colCount; i++) {
				  
				 }
				 */

				int result = this.rs.getInt(1);
				this.logger.log(String.format("Row %d results: %d", r, result));
				
				JOptionPane.showMessageDialog(
						null, String.format("%s\n-----------\nHost: %s\n-----------\n" + "Row %d results: %d",
								this.title, this.host, r, result),
						"QueryTest Results", JOptionPane.INFORMATION_MESSAGE);

			}
		} catch (SQLException e) {
			e.printStackTrace(this.logger.getPrintStream());
		}

	}

	private void runClient(Source.SourceType type, String jdbcUrl) {
		Source source = new Source(jdbcUrl, new User(this.sourceCred.getUsername(), this.sourceCred.getPassword()),
				type);
		IClient cli = new Client(source, this.logger);
		cli.query(this.sql);
		if (this.outputFile == null) {
			this.rs = cli.getResultSet();
		} else {
			cli.saveExcel(this.outputFile, true);
		}
	}

	private void driveOracle() {
		this.logger.log("Oracle source...");
		String jdbcUrl = "jdbc:oracle:thin:@ldap://" + this.ldapServer + "/" + this.host + "," + this.ldapContext;
		this.logger.log("JDBC URL: " + jdbcUrl);

		this.runClient(Source.SourceType.ORACLE, jdbcUrl);
	}

	private void driveComposite() {
		this.logger.log("Composite source...");
		String jdbcUrl = "jdbc:compositesw:dbapi@" + this.host + ":" + this.port + "?domain=" + this.domain
				+ "&dataSource=" + this.dataSource;
		this.logger.log("JDBC URL: " + jdbcUrl);

		this.runClient(Source.SourceType.COMPOSITE, jdbcUrl);
	}

	private void setCredential(String group, String title) {
		SafeWrapper sw = new SafeWrapper(this.vaultFile, new StringBuilder(this.vaultPassword));
		this.sourceCred = sw.getCredential(group, title);
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void writeJsonFile(File file) {
		JSONObject obj = new JSONObject();
		obj.put("sql", this.sql);
		obj.put("host", this.host);
		obj.put("ldapServer", this.ldapServer);
		obj.put("ldapContext", this.ldapContext);
		obj.put("domain", this.domain);
		obj.put("dataSource", this.dataSource);
		obj.put("port", this.port);
		obj.put("vaultFile", this.vaultFile);
		obj.put("vaultGroup", this.vaultGroup);
		obj.put("vaultTitle", this.vaultTitle);
		obj.put("outputFile", this.outputFile);
		obj.put("title", this.title);

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(obj.toJSONString());
		} catch (IOException e) {
			e.printStackTrace(this.logger.getPrintStream());
		}
	}

	private void readJsonFile(File file) {
		JSONParser parser = new JSONParser();

		try (Reader reader = new FileReader(file)) {
			JSONObject jsonObject = (JSONObject) parser.parse(reader);
			this.sql = (String) jsonObject.get("sql");
			this.host = (String) jsonObject.get("host");
			this.ldapServer = (String) jsonObject.get("ldapServer");
			this.ldapContext = (String) jsonObject.get("ldapContext");
			this.domain = (String) jsonObject.get("domain");
			this.dataSource = (String) jsonObject.get("dataSource");
			this.port = ((Long) jsonObject.get("port")).intValue();
			this.vaultFile = (String) jsonObject.get("vaultFile");
			this.vaultPassword = (String) jsonObject.get("vaultPassword");
			this.vaultGroup = (String) jsonObject.get("vaultGroup");
			this.vaultTitle = (String) jsonObject.get("vaultTitle");
			this.title = (String) jsonObject.get("title");
			this.outputFile = (String) jsonObject.get("outputFile");
			if (this.title == null) {
				this.title = file.getName();
			}
		} catch (IOException e) {
			e.printStackTrace(this.logger.getPrintStream());
		} catch (ParseException e) {
			e.printStackTrace(this.logger.getPrintStream());
		}
	}

}
