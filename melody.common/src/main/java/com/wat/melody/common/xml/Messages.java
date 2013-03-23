package com.wat.melody.common.xml;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.xml.messages";

	public static String NoSuchDUNIDEx_UNFOUND;

	public static String DUNIDEx_EMPTY;
	public static String DUNIDEx_INVALID;

	public static String DUNIDDocEx_FOUND_DUNID_ATTR;

	public static String DocEx_INVALID_XML_SYNTAX_AT;
	public static String DocEx_INVALID_XML_SYNTAX;
	public static String DocEx_INVALID_XML_DATA;

	public static String FilteredDocEx_INCORRECT_XPATH;
	public static String FilteredDocEx_TOO_RSTRICTIVE;
	public static String FilteredDocEx_INVALID_HERIT_ATTR_XPATH;
	public static String FilteredDocEx_INVALID_HERIT_ATTR_MANYNODEMATCH;
	public static String FilteredDocEx_INVALID_HERIT_ATTR_NONODEMATCH;
	public static String FilteredDocEx_INVALID_HERIT_ATTR_CIRCULARREF;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}