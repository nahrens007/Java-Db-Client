package com.nathanahrens.client.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.nathanahrens.client.Client;
import com.nathanahrens.client.IClient;
import com.nathanahrens.client.Source;
import com.nathanahrens.client.User;
import com.nathanahrens.log.Logger;
import com.nathanahrens.pwsafe.Credential;
import com.nathanahrens.pwsafe.SafeWrapper;

public class DbCliClient {

	private String sqlFile;
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
	private String outputFile;
	private boolean showStatus;
	private Logger logger;
	private String logFile;
	private boolean help;
	
	public DbCliClient() {
		this.logger = new Logger();
	}
	
	private String getSqlFromFile(String filePath) {
		try {
			return Files.readString(Path.of(filePath));
		} catch (IOException e) {
			e.printStackTrace(this.logger.getPrintStream());
			System.exit(-1);
		}
		return null;
	}
	
	private void runClient(Source.SourceType type,String jdbcUrl) {
		Source source = new Source(jdbcUrl, new User(this.sourceCred.getUsername(), this.sourceCred.getPassword()),
				type);
		IClient cli = new Client(source,this.logger);
		cli.query(getSqlFromFile(this.sqlFile));
		cli.saveExcel(this.outputFile, true);
	}

	public void driveOracle() {
		this.logger.log("Oracle source...");
		String jdbcUrl = "jdbc:oracle:thin:@ldap://" + this.ldapServer + "/" + this.host + "," + this.ldapContext;
		this.logger.log("JDBC URL: " + jdbcUrl);

		this.runClient(Source.SourceType.ORACLE, jdbcUrl);
	}

	public void driveComposite() {
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

	public void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		
		try {
			parser.parseArgument(args);
			if (this.help) {
				this.logger.log("This app provides 2 different modes to operate from: CLI and QueryTest.");
				this.logger.log("CLI mode means you must provide all the details of the query to run (and output) as command line arguments.");
				this.logger.log("QueryTest mode means the details of the query to run and the output are described in .json files in the local directory.");
				this.logger.log("Usage:");
				parser.printUsage(this.logger.getPrintStream());
			} else if (args.length < 1 || this.showStatus) {
				// Run QueryTest version
				LinkedList<String> arr = new LinkedList<String>();
				if (this.showStatus) {
					arr.add("--showStatus");
				}
				if (this.logFile != null) {
					arr.add("--log");
					arr.add(this.logFile);
				}
				QueryTestDriver.main(arr.toArray(new String[arr.size()]));
			} else {
				// Run CLI version
				if (this.logFile != null) {
					this.logger.setLogFile(new File(this.logFile));
				}
				this.execute();
			}
		} catch (CmdLineException e) {
			this.logger.log("ERROR: Unable to parse command line options: " + e);
			this.logger.log("Usage:");
			parser.printUsage(this.logger.getPrintStream());
		} catch (FileNotFoundException e) {
			this.logger.log("ERROR: Unable to set the log file!");
			e.printStackTrace(this.logger.getPrintStream());
		}
	}

	private void execute() {
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
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		DbCliClient client = new DbCliClient();
		client.parseArgs(args);
	}

	@Option(name = "--sqlFile", depends= {"--host","--vaultFile","--vaultPassword","--vaultPassword","--vaultTitle","--outputFile"}, usage = "Required: Set the file to be used as the SQL to query the source.")
	public void setSqlFile(String sqlFilePath) {
		this.sqlFile = sqlFilePath;
	}

	@Option(name = "--host", depends= {"--sqlFile","--vaultFile","--vaultPassword","--vaultPassword","--vaultTitle","--outputFile"}, usage = "Required: Set the host of the source.")
	public void setHost(String host) {
		this.host = host;
	}

	@Option(name = "--ldapServer", depends = { "--ldapContext","--host" }, forbids = { "--domain", "--dataSource",
			"--port" }, usage = "Oracle: Set the LDAP server to be used to determine the host source. Only for Oracle sources.")
	public void setLdapServer(String ldapServer) {
		this.ldapServer = ldapServer;
	}

	@Option(name = "--ldapContext", depends = { "--ldapServer","--host" }, forbids = { "--domain", "--dataSource",
			"--port" }, usage = "Oracle: Set the LDAP context to be used with the LDAP server, for determining the host source. Only for Oracle sources.")
	public void setLdapContext(String ldapContext) {
		this.ldapContext = ldapContext;
	}

	@Option(name = "--domain", depends = { "--dataSource", "--port","--host" }, forbids = { "--ldapServer",
			"--ldapContext" }, usage = "Composite: Set the domain to use when connecting to a composite source..")
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Option(name = "--dataSource", depends = { "--port", "--domain","--host" }, forbids = { "--ldapServer",
			"--ldapContext" }, usage = "Composite: Set the data source to use when connecting to a composite source..")
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	@Option(name = "--port", depends = { "--dataSource", "--domain","--host" }, forbids = { "--ldapServer",
			"--ldapContext" }, usage = "Composite: Set the port to use when connecting to a composite source..")
	public void setPort(int port) {
		this.port = port;
	}

	@Option(name = "--vaultFile", depends= {"--host","--sqlFile","--vaultPassword","--vaultPassword","--vaultTitle","--outputFile"}, usage = "Required: Set the file path of the password vault (.psafe3).")
	public void setVaultFile(String vaultFile) {
		this.vaultFile = vaultFile;
	}

	@Option(name = "--vaultPassword", depends= {"--host","--vaultFile","--vaultPassword","--sqlFile","--vaultTitle","--outputFile"}, usage = "Required: Set the password to access the password vault.")
	public void setVaultPassword(String vaultPassword) {
		this.vaultPassword = vaultPassword;
	}

	@Option(name = "--vaultGroup", usage = "Optional: Set the group within the vault. Not required, in the case that the record you intend to retrieve is in the root of the vault.")
	public void setVaultGroup(String vaultGroup) {
		this.vaultGroup = vaultGroup;
	}

	@Option(name = "--vaultTitle", depends= {"--sqlFile","--vaultFile","--vaultPassword","--vaultPassword","--host","--outputFile"}, usage = "Required: Set the title of the record to retrieve from the vault.")
	public void setVaultTitle(String vaultTitle) {
		this.vaultTitle = vaultTitle;
	}

	@Option(name = "--outputFile", depends= {"--sqlFile","--vaultFile","--vaultPassword","--vaultPassword","--vaultTitle","--host"}, usage = "Required: Set the output file XLSX to write the SQL results to (.xlsx).")
	public void setOutputFile(String filePath) {
		this.outputFile = filePath;
	}
	
	@Option(name = "--showStatus",usage="Show a status message when app starts to show that it is running. Only used when executing in QueryTest mode (using .json files).")
	public void setShowStatus(boolean status) {
		this.showStatus = status;
	}
	
	@Option(name = "--log",usage="Set log file. For CLI mode, this should be a file (i.e., dbclient.log). For QueryTest mode, this should be a directory to write logs to.")
	public void setLogFile(String filePath) {
		this.logFile = filePath;
	}
	
	@Option(name="--help",usage="Display help message")
	public void setHelp(boolean help) {
		this.help = help;
	}
}
