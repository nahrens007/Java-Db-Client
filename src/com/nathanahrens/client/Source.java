package com.nathanahrens.client;

/**
 * Class defining a source (such as source of database). 
 * @author nahrens
 *
 */
public class Source {
	private User user;
	private String sourceURL;
	private SourceType type;

	/**
	 * Defines the driver to use when connecting to the source.
	 */
	public static enum SourceType {
		ORACLE, COMPOSITE
	}

	/**
	 * 
	 * @param sourceURL JDBC URL to use when connecting to the source.
	 * @param user User to use when connecting to the source.
	 * @param type Type of driver to use when connecting to the source.
	 */
	public Source(String sourceURL, User user, SourceType type) {
		this.user = user;
		this.sourceURL = sourceURL;
		this.type = type;
	}

	/**
	 * 
	 * @return Return the {@link User}
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * 
	 * @return Return the JDBC URL of the source.
	 */
	public String getSourceURL() {
		return this.sourceURL;
	}

	/**
	 * 
	 * @return Return the {@link SourceType} of the source.
	 */
	public SourceType getSourceType() {
		return this.type;
	}

	/**
	 * 
	 * @return Return the driver class depending on the type of source.
	 */
	public String getSourceTypeDriver() {
		switch (this.type) {
		case ORACLE:
			System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
			return "oracle.jdbc.driver.OracleDriver";
		case COMPOSITE:
			return "cs.jdbc.driver.CompositeDriver";
		default:
			return null;
		}
	}

}
