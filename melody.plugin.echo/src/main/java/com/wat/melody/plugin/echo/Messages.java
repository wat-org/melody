package com.wat.melody.plugin.echo;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.plugin.echo.messages";

	public static String EchoEx_PARENT_DIR_NOT_EXISTS;
	public static String EchoEx_FAILED_TO_CRAETE_PARENT_DIR;
	public static String EchoEx_IO_ERROR;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
