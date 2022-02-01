package com.nathanahrens.client;

import java.sql.ResultSet;

/**
 * <p>Interface to encapsulate a source client (such as a client to a database).</p>
 * <p>It is expected that, once instantiated, a client must execute {@link #query(String)} prior to calling any other action (such as {@link #saveExcel(String)}).<p>
 * <p>Only one action should be called per instance of an {@link IClient} (that is, after {@link #query(String)} has been called). That is because
 * a {@link ResultSet} can only be iterated one time.</p>
 * @author nahrens
 *
 */
public interface IClient {

	/**
	 * Send query to source to obtain {@link ResultSet} with data.
	 * @param sql SQL query to run in source.
	 * @return boolean True if query was successful, else false.
	 */
	public boolean query(String sql);
	
	/**
	 * Write the {@link ResultSet} the instance obtained from {@link #query(String)} to an Excel file. 
	 * @param filePath Path of the Excel file to write.
	 * @param saveSql If true, adds a sheet to the Excel file with the query SQL.
	 */
	public void saveExcel(String filePath, boolean saveSql);
	
	/**
	 * Write the {@link ResultSet} the instance obtained from {@link #query(String)} to an Excel file. 
	 * @param filePath Path of the Excel file to write.
	 */
	public void saveExcel(String filePath);
	
	/**
	 * Write the {@link ResultSet} the instance obtained from {@link #query(String)} to stdout. 
	 */
	public void print();
	
	/**
	 * 
	 * @return ResultSet ResultSet of executed query.
	 */
	public ResultSet getResultSet();
}
