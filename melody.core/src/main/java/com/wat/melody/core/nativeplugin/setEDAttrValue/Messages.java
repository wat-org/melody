package com.wat.melody.core.nativeplugin.setEDAttrValue;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.setEDAttrValue.messages";

	public static String TargetAttrEx_NOT_XPATH;
	public static String TargetAttrEx_MATCH_NO_NODE;
	public static String TargetAttrEx_MATCH_MANY_NODES;
	public static String TargetAttrEx_NOT_MATCH_ELEMENT;

	public static String AttributeAttrEx_EMPTY;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
