package com.nathanahrens.client.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import com.nathanahrens.log.Logger;

public class QueryTestDriver {
	private static File logDir;
	private static String getLogFileTimestamp() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
	}

	/**
	 * 
	 * @param args --showStatus will show a status window when this first starts.
	 *             --log VAL will write log files to the VAL directory.
	 */
	public static void main(String[] args) {
		Logger logger = new Logger();
		for (int i = 0; i < args.length; i++) {
			if (args[i] == "--showStatus") {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(null, "The QueryTest has started.");
					}
				});
				thread.start();
			} else if (args[i] == "--log" && i + 1 < args.length) {
				logDir = new File(args[i + 1]);
				if (!logDir.isDirectory()) {
					logDir = null;
					logger.log(logDir.toString() + " is not a directory, could not set log files to that directory.");
				}
			}
		}
		// Set log file if the argument was passed in and is valid.
		if (logDir != null) {
			String logFile = "QueryTestDriver_" + getLogFileTimestamp() + ".log";
			try {
				logger.setLogFile(new File(logDir, logFile));
			} catch (FileNotFoundException e) {
				logger.log("Could not set log file to " + logFile);
				e.printStackTrace(logger.getPrintStream());
			}
		}

		/* FileFilter to list only files with .json extension */
		FileFilter jsonFileFilter = new FileFilter() {
			public boolean accept(File file) {
				if (file.getName().toLowerCase().endsWith(".json")) {
					return true;
				}
				return false;
			}
		};

		/* get .json file listing in current directory */
		File dir = new File(".");
		File[] listing = dir.listFiles(jsonFileFilter);
		if (listing != null) {
			for (File child : listing) {
				System.out.println(child);
				if (child.isFile()) {
					/* start new thread to run the QueryTest represented by the json file. */
					Thread thread = new Thread(new Runnable() {
						public void run() {
							Logger childLogger = new Logger();
							if (logDir != null) {
								try {
									String logFile = "QueryTest_" + child.getName() + "_" + getLogFileTimestamp() + ".log";
									logger.log("Set logger file for " + child.toString() + " to " + logFile);
									childLogger.setLogFile(new File(logDir,logFile));
								} catch (FileNotFoundException e) {
									e.printStackTrace(logger.getPrintStream());
								}
							}
							new QueryTest(child, childLogger);
						}
					});
					thread.start();
				}
			}
		}
	}

}
