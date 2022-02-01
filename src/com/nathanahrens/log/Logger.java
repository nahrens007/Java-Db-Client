package com.nathanahrens.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Logger {
	private PrintStream printer = System.out; 
	
	public void log(String message) {
		printer.println(message);
	}
	
	public void setLogFile(File file) throws FileNotFoundException {
		printer = new PrintStream(file);
	}
	
	public void setPrintStream(PrintStream ps) {
		printer = ps;
	}
	
	public PrintStream getPrintStream() {
		return this.printer;
	}
	
}
