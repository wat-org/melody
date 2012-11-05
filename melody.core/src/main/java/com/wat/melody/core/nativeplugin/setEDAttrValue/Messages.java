package com.wat.melody.core.nativeplugin.setEDAttrValue;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.core.nativeplugin.setEDAttrValue.messages";

	public static String SetEDAttrEx_UPDATE_ED_ITEMS_EMPTY;
	public static String SetEDAttrEx_UPDATE_ED_ITEMS_INVALID_XPATH;
	public static String SetEDAttrEx_UPDATE_ED_ITEMS_INVALID_TARGET;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
