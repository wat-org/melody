package com.wat.melody.common.utils;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.utils.messages";

	public static String NoSuchDUNIDEx_UNFOUND;

	public static String TarGzEx_NOT_A_TARGZ;
	public static String TarGzEx_INVALID_EXTENSION;

	public static String PropertyNameEx_EMPTY;
	public static String PropertyNameEx_INVALID;

	public static String PropertyEx_EMPTY;
	public static String PropertyEx_INVALID;

	public static String PropertiesSetEx_MALFORMED_LINE;
	public static String PropertiesSetEx_MULTIPLE_DIRECTIVE;
	public static String PropertiesSetEx_INVALID_PROPERTY_VALUE;
	public static String PropertiesSetEx_INVALID_ESCAPE_SEQUENCE;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_FOUND;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_OPENED;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_CLOSED;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_UNDEFINED;
	public static String PropertiesSetEx_CIRCULAR_REFERENCE;

	public static String OrderNameSetEx_EMPTY_ORDER_NAME;
	public static String OrderNameSetEx_INVALID_ORDER_NAME;
	public static String OrderNameSetEx_EMPTY;

	public static String OrderNameEx_EMPTY;
	public static String OrderNameEx_INVALID;

	public static String LogThresholdEx_EMPTY_STRSTR;
	public static String LogThresholdEx_INVALID_STRSTR;
	public static String LogThresholdEx_MAX_REACHED;
	public static String LogThresholdEx_MIN_REACHED;

	public static String DUNIDEx_EMPTY;
	public static String DUNIDEx_INVALID;

	public static String DUNIDDocEx_FOUND_DUNID_ATTR;

	public static String FilterSetEx_EMPTY_FILTER;
	public static String FilterSetEx_INVALID_FILTER;
	public static String FilterSetEx_EMPTY;

	public static String FilterEx_EMPTY;
	public static String FilterEx_INCORRECT_XPATH;
	public static String FilterEx_TOO_RSTRICTIVE;

	public static String FileEx_NOT_A_FILE;
	public static String FileEx_CANT_READ;
	public static String FileEx_CANT_WRITE;
	public static String FileEx_NOT_FOUND;

	public static String DirEx_NOT_A_DIR;
	public static String DirEx_CANT_READ;
	public static String DirEx_CANT_WRITE;
	public static String DirEx_NOT_FOUND;
	public static String DirEx_INVALID_PARENT;

	public static String DocEx_INVALID_XML_SYNTAX_AT;
	public static String DocEx_INVALID_XML_SYNTAX;
	public static String DocEx_INVALID_XML_DATA;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
