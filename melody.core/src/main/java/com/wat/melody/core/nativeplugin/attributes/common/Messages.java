package com.wat.melody.core.nativeplugin.attributes.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.attributes.common.messages";

	public static String TargetAttrEx_NOT_XPATH;
	public static String TargetAttrEx_MATCH_NO_NODE;
	public static String TargetAttrEx_MATCH_MANY_NODES;
	public static String TargetAttrEx_NOT_MATCH_ELEMENT;

	public static String AttributeNameEx_EMPTY;
	public static String AttributeNameEx_INVALID;
	public static String AttributeNameEx_DUNID_FORBIDDEN;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
