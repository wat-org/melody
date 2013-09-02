package com.wat.melody.plugin.xml.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.xml.common.messages";

	public static String ElementsSelectorEx_NOT_XPATH;
	public static String ElementsSelectorEx_NOT_MATCH_ELEMENT;
	public static String ConditionEx_NOT_XPATH;
	public static String NodeContentEx_DUPLICATE_DECLARATION;
	public static String NodeContentEx_NOT_XML;
	
	public static String XmlFileEx_ERROR_WHILE_LOADING;
	public static String XmlFileEx_IO_WHILE_SAVING;
	
	public static String ApplyMsg_CONDITION_NOT_MATCH;
	public static String ApplyMsg_NO_MATCH;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}