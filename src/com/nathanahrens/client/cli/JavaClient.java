package com.nathanahrens.client.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.nathanahrens.client.Client;
import com.nathanahrens.client.IClient;
import com.nathanahrens.client.Source;
import com.nathanahrens.client.User;
import com.nathanahrens.log.Logger;
import com.nathanahrens.pwsafe.Credential;
import com.nathanahrens.pwsafe.SafeWrapper;

public class JavaClient {

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
	private Credential sourceCred;
	private ResultSet rs;

	private void evaluateResultSet() {
		int colCount;
		try {
			colCount = this.rs.getMetaData().getColumnCount();
			if (colCount != 1) {
				System.out.println("Invalid query - must return 1 column of type number!");
				return;
			}
			for (int r = 1; this.rs.next(); r++) {
				
				/*
				 * If you wanted to loop through columns
				for (int i = 0; i < colCount; i++) {
					
				}
				*/
				
				int result = this.rs.getInt(1);
				System.out.println(String.format("Row %d results: %d", r,result));
				JOptionPane.showMessageDialog(null, String.format("Host: %s\n-----------\n"
						+ "Row %d results: %d", this.host,r,result));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void runClient(Source.SourceType type, String jdbcUrl) {
		Source source = new Source(jdbcUrl, new User(this.sourceCred.getUsername(), this.sourceCred.getPassword()),
				type);
		IClient cli = new Client(source,new Logger());
		cli.query(this.sql);
		this.rs = cli.getResultSet();
	}

	public void driveOracle() {
		System.out.println("Oracle source...");
		String jdbcUrl = "jdbc:oracle:thin:@ldap://" + this.ldapServer + "/" + this.host + "," + this.ldapContext;
		System.out.println("JDBC URL: " + jdbcUrl);

		this.runClient(Source.SourceType.ORACLE, jdbcUrl);
	}

	public void driveComposite() {
		System.out.println("Composite source...");
		String jdbcUrl = "jdbc:compositesw:dbapi@" + this.host + ":" + this.port + "?domain=" + this.domain
				+ "&dataSource=" + this.dataSource;
		System.out.println("JDBC URL: " + jdbcUrl);

		this.runClient(Source.SourceType.COMPOSITE, jdbcUrl);
	}

	private void setCredential(String group, String title) {
		SafeWrapper sw = new SafeWrapper(this.vaultFile, new StringBuilder(this.vaultPassword));
		this.sourceCred = sw.getCredential(group, title);
	}

	public void parseArgs() {
		this.sql = "select count(*)\r\n" + "  from jmsuser.jms_scm_invoice_item\r\n" + " where 1=1\r\n"
				+ "   and jms_item_nb in ('7180200574','7180200822')\r\n" + "   and invoice_id in (\r\n"
				+ "select invoice_id\r\n" + "  from jmsuser.jms_Scm_invoice_loc l\r\n"
				+ " where location_type = 'Ship To'\r\n" + "   and l.location_id = '2489580680020'\r\n"
				+ "   and l.invoice_id in (\r\n" + "SELECT invoice_id FROM jmsuser.jms_scm_invoice\r\n"
				+ "where scm_trx_id in (\r\n" + "SELECT scm_trx_id FROM \r\n" + "jmsuser.jms_scm_trx\r\n"
				+ "where partner_send_recv_id = 'LCLPRODEDI'\r\n"
				+ "and created_date > to_date('1/11/2022 16:30:00', 'MM/DD/YYYY HH24:MI:SS')\r\n"
				+ "and trx_type = 'INV')))";
		this.host = "aomprod";
		this.ldapServer = "orrproda.na.jmsmucker.com:389";
		this.ldapContext = "cn=OracleContext,dc=na,dc=jmsmucker,dc=com";
		/*
		 * this.domain = null; this.dataSource = null; this.port = null;
		 */
		this.vaultFile = "pwsafe.psafe3";
		this.vaultPassword = "BROS4life!";
		this.vaultGroup = "Database.AOM";
		this.vaultTitle = "AOM PROD";
	}

	public void execute() {
		this.setCredential(this.vaultGroup, this.vaultTitle);
		if (this.ldapServer == null && this.domain == null) {
			System.out.println("Must provide details for either Oracle source or Composite source.");
			System.exit(-1);
		}
		if (this.domain != null) {
			this.driveComposite();
		} else {
			this.driveOracle();
		}
		this.evaluateResultSet();
	}

	public static void main(String[] args) {
		/* copy "C:\Users\nahrens\Documents\My Safes\nahrens_pwsafe.psafe3" pwsafe.psafe3 */
		
		try {
			Files.copy(Paths.get("C:\\Users\\nahrens\\Documents\\My Safes\\nahrens_pwsafe.psafe3"), Paths.get("pwsafe.psafe3"),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JavaClient client = new JavaClient();
		client.parseArgs();
		client.execute();
	}
}
