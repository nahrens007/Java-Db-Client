package com.nathanahrens.resultset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ResultSetUtil {
	/**
	 * 
	 * @param rs        {@link ResultSet} to print headers
	 * @param separator String to use as column separator
	 * @param divider   Include a divider to separate header from content?
	 * @throws SQLException
	 */
	public static void printHeaders(ResultSet rs, String separator, boolean divider) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		int lengths[] = new int[colCount + 1];

		for (int i = 1; i <= colCount; i++) {
			String colName = rsmd.getColumnName(i);
			lengths[i] = colName.length();
			System.out.print(colName + separator);
		}
		System.out.println();

		if (divider) {
			for (int i = 1; i <= colCount; i++) {
				for (int j = 0; j < lengths[i]; j++) {
					System.out.print("-");
				}
				System.out.print(separator);
			}
			System.out.println();
		}
	}

	public static void printResultSet(ResultSet rs, String separator) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();

			printHeaders(rs, separator, true);
			while (rs.next()) {
				for (int i = 1; i <= colCount; i++) {
					System.out.print(rs.getString(i) + separator);
				}
				System.out.println();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to parse ResultSet to print...");
		}
	}

	/**
	 * Saves a {@link ResultSet} to an Excel file.
	 * 
	 * @param rs       Result set to save Excel file from.
	 * @param filePath File path to save Excel file to.
	 * @see net.sourceforge.squirrel_sql.fw.gui.action.fileexport.DataExportExcelWriter
	 */
	public static void saveExcel(ResultSet rs, String filePath) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet spreadsheet = workbook.createSheet("Sheet1");
		XSSFRow row = spreadsheet.createRow(0);
		XSSFCell cell;

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();

			// write header row
			for (int i = 1; i <= colCount; i++) {
				cell = row.createCell(i - 1);
				cell.setCellValue(rsmd.getColumnName(i));
			}
			// write data row
			for (int r = 1; rs.next(); r++) {
				XSSFRow dataRow = spreadsheet.createRow(r);
				for (int i = 0; i < colCount; i++) {
					dataRow.createCell(i).setCellValue(rs.getString(i + 1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to parse ResultSet to Excel...");
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(filePath));
			workbook.write(out);
			workbook.close();
			out.close();
			rs.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("Unable to write to file path: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to write Excel file...");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to close the ResultSet...");
		}

	}
}
