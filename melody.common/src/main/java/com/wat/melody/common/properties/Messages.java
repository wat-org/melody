package com.wat.melody.common.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.properties.messages";

	public static String PropertiesSetEx_MALFORMED_LINE;
	public static String PropertiesSetEx_MULTIPLE_DIRECTIVE;
	public static String PropertiesSetEx_INVALID_PROPERTY_VALUE;
	public static String PropertiesSetEx_INVALID_ESCAPE_SEQUENCE;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_FOUND;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_OPENED;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_NOT_CLOSED;
	public static String PropertiesSetEx_VARIABLE_SEQUENCE_UNDEFINED;
	public static String PropertiesSetEx_CIRCULAR_REFERENCE;

	public static String PropertyEx_EMPTY;
	public static String PropertyEx_INVALID;

	public static String PropertyNameEx_EMPTY;
	public static String PropertyNameEx_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}