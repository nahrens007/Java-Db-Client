/*
 * $Id: Log.java 488 2013-12-11 23:22:25Z roxon $
 * 
 * Copyright (c) 2008-2014 David Muller <roxon@users.sourceforge.net>.
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pwsafe.lib;

/**
 * This class provides logging facilities using commons logging.
 * 
 * @author Kevin Preece
 */
public class Log {
	private int debugLevel;
	private boolean debugEnabled = false;

	static {
		// DOMConfigurator.configure( "log-config.xml" );
	}

	private Log(final String name) {
		// logger = Logger.getLogger( name );
		// logger = LogFactory.getLog(name);

		setDebugLevel(3);
	}

	/**
	 * Returns an instance of <code>Log</code> for the logger named
	 * <code>name</code>.
	 * 
	 * @param name the logger name.
	 * 
	 * @return An <code>Log</code> instance.
	 */
	public static Log getInstance(final String name) {
		return new Log(name);
	}

	/**
	 * Returns an instance of <code>Log</code> for the logger named
	 * <code>Class.name</code>.
	 * 
	 * @param name the logger name.
	 * 
	 * @return An <code>Log</code> instance.
	 */
	public static Log getInstance(final Class aClass) {
		return new Log(aClass.getName());
	}

	public boolean isDebugEnabled() {
		return this.debugEnabled;
	}

	public void debug(String msg) {
		this.debug1(msg);
	}

	/**
	 * Writes a message at debug level 1
	 * 
	 * @param msg the message to issue.
	 */
	public void debug1(final String msg) {
		if (isDebug1Enabled()) {
			System.out.println("debug1: " + msg);
		}
	}

	/**
	 * Writes a message at debug level 2
	 * 
	 * @param msg the message to issue.
	 */
	public void debug2(final String msg) {
		if (isDebug2Enabled()) {
			System.out.println("debug2: " + msg);
		}
	}

	/**
	 * Writes a message at debug level 3
	 * 
	 * @param msg the message to issue.
	 */
	public void debug3(final String msg) {
		if (isDebug3Enabled()) {
			System.out.println("debug3: " + msg);
		}
	}

	/**
	 * Writes a message at debug level 4
	 * 
	 * @param msg the message to issue.
	 */
	public void debug4(final String msg) {
		if (isDebug4Enabled()) {
			System.out.println("debug4: " + msg);
		}
	}

	/**
	 * Writes a message at debug level 5
	 * 
	 * @param msg the message to issue.
	 */
	public void debug5(final String msg) {
		if (isDebug5Enabled()) {
			System.out.println("debug5: " + msg);
		}
	}

	/**
	 * Logs entry to a method.
	 * 
	 * @param method the method name.
	 */
	public void enterMethod(String method) {
		if (!method.endsWith(")")) {
			method = method + "()";
		}
		if (this.debugEnabled) {
			System.out.println("Entering method " + method);
		}
	}

	/**
	 * Writes a message at error level
	 * 
	 * @param msg the message to issue.
	 */
	public void error(final String msg) {
		System.out.println("ERROR: " + msg);
	}

	/**
	 * Writes a message at error level along with details of the exception
	 * 
	 * @param msg    the message to issue.
	 * @param except the exeption to be logged.
	 */
	public void error(final String msg, final Throwable except) {
		System.out.println(msg + except);
	}

	/**
	 * Logs the exception at a level of error.
	 * 
	 * @param except the <code>Exception</code> to log.
	 */
	public void error(final Throwable except) {
		System.out.println("An Exception has occurred " + except);
	}

	/**
	 * Returns the current debug level.
	 * 
	 * @return Returns the debugLevel.
	 */
	public int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * Writes a message at info level
	 * 
	 * @param msg the message to issue.
	 */
	public void info(final String msg) {
		System.out.println("INFO: " + msg);
	}

	/**
	 * Returns <code>true</code> if debuuging at level 1 is enabled,
	 * <code>false</code> if it isn't.
	 * 
	 * @return <code>true</code> if debuuging at level 1 is enabled,
	 *         <code>false</code> if it isn't.
	 */
	public boolean isDebug1Enabled() {
		return this.debugEnabled;
	}

	/**
	 * Returns <code>true</code> if debuuging at level 2 is enabled,
	 * <code>false</code> if it isn't.
	 * 
	 * @return <code>true</code> if debuuging at level 2 is enabled,
	 *         <code>false</code> if it isn't.
	 */
	public boolean isDebug2Enabled() {
		return this.isDebug1Enabled() && (debugLevel >= 2);
	}

	/**
	 * Returns <code>true</code> if debuuging at level 3 is enabled,
	 * <code>false</code> if it isn't.
	 * 
	 * @return <code>true</code> if debuuging at level 3 is enabled,
	 *         <code>false</code> if it isn't.
	 */
	public boolean isDebug3Enabled() {
		return this.isDebug1Enabled() && (debugLevel >= 3);
	}

	/**
	 * Returns <code>true</code> if debuuging at level 4 is enabled,
	 * <code>false</code> if it isn't.
	 * 
	 * @return <code>true</code> if debuuging at level 4 is enabled,
	 *         <code>false</code> if it isn't.
	 */
	public boolean isDebug4Enabled() {
		return this.isDebug1Enabled() && (debugLevel >= 4);
	}

	/**
	 * Returns <code>true</code> if debuuging at level 5 is enabled,
	 * <code>false</code> if it isn't.
	 * 
	 * @return <code>true</code> if debuuging at level 5 is enabled,
	 *         <code>false</code> if it isn't.
	 */
	public boolean isDebug5Enabled() {
		return this.isDebug1Enabled() && (debugLevel >= 5);
	}

	/**
	 * Logs exit from a method.
	 * 
	 * @param method the method name.
	 */
	public void leaveMethod(String method) {
		if (this.isDebug1Enabled()) {
			if (!method.endsWith(")")) {
				method = method + "()";
			}
			System.out.println("Leaving method " + method);
		}
	}

	/**
	 * Sets the debug level.
	 * 
	 * @param debugLevel The debugLevel to set.
	 */
	public void setDebugLevel(int debugLevel) {
		if (debugLevel < 1) {
			debugLevel = 1;
		} else if (debugLevel > 5) {
			debugLevel = 5;
		}
		this.debugLevel = debugLevel;
	}

	/**
	 * Logs a message at the warning level.
	 * 
	 * @param msg the message to issue.
	 */
	public void warn(final String msg) {
		System.out.println("WARN: " + msg);
	}

}
