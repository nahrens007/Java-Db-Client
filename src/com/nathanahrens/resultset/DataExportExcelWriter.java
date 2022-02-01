package com.nathanahrens.resultset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataExportExcelWriter {
	private HashMap<String, XSSFCellStyle> formatCache = null;
	private XSSFWorkbook workbook = new XSSFWorkbook();

	/*
	 * note POI which we use for the excel export has a limit of 4000 styles
	 * therefore formatCache has been introduced to re-use styles across cells.
	 */
	private void makeTemporalCell(XSSFCell retVal, Date cellObj, String format) {
		XSSFCreationHelper creationHelper = workbook.getCreationHelper();
		XSSFCellStyle cellStyle;
		if (formatCache == null) {
			cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(format));
			formatCache = new HashMap<String, XSSFCellStyle>();
			formatCache.put(format, cellStyle);
		} else {
			cellStyle = formatCache.get(format);
			if (cellStyle == null) {
				cellStyle = workbook.createCellStyle();
				cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(format));
				formatCache.put(format, cellStyle);
			}
		}
		retVal.setCellStyle(cellStyle);
		if (null != cellObj) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date) cellObj);
			retVal.setCellValue(calendar);
		}
	}

	private String getDataXLSAsString(Object cellObj) {
		if (cellObj == null) {
			return "";
		} else {
			return cellObj.toString().trim();
		}
	}

	/**
	 * Saves a {@link ResultSet} to an Excel file.
	 * 
	 * @param rs       Result set to save Excel file from.
	 * @param filePath File path to save Excel file to.
	 * @param saveSql  If true, will add a sheet and write the SQL query to it.
	 * @param sql      If saveSql is true, will use this as the query to add to the
	 *                 new sheet "SQL Statment".
	 * @throws FileNotFoundException When unable to open Excel file to write to.
	 * @throws IOException           When unable to write to Excel file or save
	 *                               file.
	 * @throws SQLException          When unable to parse ResultSet or close
	 *                               ResultSet.
	 * @see net.sourceforge.squirrel_sql.fw.gui.action.fileexport.DataExportExcelWriter
	 */
	public void saveExcel(ResultSet rs, String filePath, boolean saveSql, String sql) throws IOException, SQLException {

		XSSFSheet spreadsheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("Sheet1"));
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

			// write data rows
			for (int r = 1; rs.next(); r++) {
				// Create new row in sheet
				XSSFRow dataRow = spreadsheet.createRow(r);

				// Create each column in the row
				for (int i = 0; i < colCount; i++) {

					Object cellObj = rs.getObject(i + 1);
					cell = dataRow.createCell(i);

					// If cellObj is null, set to empty string "", otherwise methods will fail
					// ("intValue()","shortValue()", etc...)
					if (cellObj == null) {
						cell.setCellValue(getDataXLSAsString(cellObj));
						continue;
					}

					// cellObj not null, set appropriate data type for Excel.
					switch (rsmd.getColumnType(i + 1)) {
					case Types.BIT:
					case Types.BOOLEAN:
						cell.setCellValue((Boolean) cellObj);
						break;
					case Types.INTEGER:
						cell.setCellValue(((Number) cellObj).intValue());
						break;
					case Types.SMALLINT:
					case Types.TINYINT:
						cell.setCellValue(((Number) cellObj).shortValue());
						break;
					case Types.NUMERIC:
					case Types.DECIMAL:
					case Types.FLOAT:
					case Types.DOUBLE:
					case Types.REAL:
						cell.setCellValue(((Number) cellObj).doubleValue());
						break;
					case Types.BIGINT:
						cell.setCellValue(Long.parseLong(cellObj.toString()));
						break;
					case Types.DATE:
						makeTemporalCell(cell, (Date) cellObj, "m/d/yy");
						break;
					case Types.TIMESTAMP:
						makeTemporalCell(cell, (Date) cellObj, "m/d/yy h:mm");
						break;
					case Types.TIME:
						makeTemporalCell(cell, (Date) cellObj, "h:mm");
						break;
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
					default:
						cell.setCellValue(getDataXLSAsString(cellObj));
					}
				}
			}

			if (saveSql) {
				spreadsheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("SQL Statement"));
				row = spreadsheet.createRow(0);
				cell = row.createCell(0);
				cell.setCellValue(sql);
			}
		} catch (SQLException e) {
			throw e;
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(filePath));
			workbook.write(out);
			workbook.close();
			out.close();
			rs.close();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		}

	}
}
