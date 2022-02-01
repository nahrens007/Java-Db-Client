package com.nathanahrens.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.nathanahrens.log.Logger;
import com.nathanahrens.resultset.DataExportExcelWriter;
import com.nathanahrens.resultset.ResultSetUtil;

public class Client implements IClient {

	private final Source source;
	private Connection connection;
	private ResultSet rs;
	private String sql;
	private Logger logger;

	public Client(Source source, Logger logger) {
		this.logger = logger;
		this.logger.log("Creating client...");
		this.source = source;
	}

	private boolean connect() {
		this.logger.log("Connecting to source...");
		try {
			this.connection = this.getConnection();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			this.logger.log("Could not create connection to " + this.source.getSourceURL());
		}
		return false;
	}

	private Connection getConnection() throws SQLException {
		if (this.connection == null) {
			try {
				Class.forName(this.source.getSourceTypeDriver());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				this.logger.log("Please ensure the driver class is in the classpath...");
			}
			Connection conn = DriverManager.getConnection(this.source.getSourceURL(),
					this.source.getUser().getUserName(), this.source.getUser().getPassword());
			return conn;
		}
		return this.connection;
	}

	public boolean query(String sql) {
		this.sql = sql;
		if (this.connect()) {
			this.logger.log("Sending query to source...");
			PreparedStatement stmt;
			try {
				stmt = this.connection.prepareStatement(sql);
				
				// Execute query
				long startTime = System.nanoTime();
				stmt.executeQuery();
				long endTime = System.nanoTime();
				double delta = (double) ((endTime - startTime)/1000000000.0);
				this.logger.log(String.format("Query executed in %,.3f seconds... ",delta));
				
				// Fetch ResultSet
				this.rs = stmt.getResultSet();
				
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				this.logger.log("Failed to execute query...");
			}
		}
		return false;
	}

	public void saveExcel(String path) {
		this.saveExcel(path, false);
	}
	
	public ResultSet getResultSet() {
		return this.rs;
	}

	public void saveExcel(String path, boolean saveSql) {
		this.logger.log("Saving to Excel...");
		DataExportExcelWriter excel = new DataExportExcelWriter();
		try {
			long startTime = System.nanoTime();
			excel.saveExcel(rs, path, saveSql, this.sql);
			long endTime = System.nanoTime();
			double delta = (double) ((endTime - startTime)/1000000000.0);
			this.logger.log(String.format("Excel file written successfully in %,.3f seconds: %s",delta,path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			this.logger.log("Unable to open file...");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			this.logger.log("Unable to save file...");
			System.exit(-1);
		} catch (SQLException e) {
			e.printStackTrace();
			this.logger.log("Unable to parse ResultSet...");
			System.exit(-1);
		}
	}

	public void print() {
		ResultSetUtil.printResultSet(this.rs, "\t");
	}
}
