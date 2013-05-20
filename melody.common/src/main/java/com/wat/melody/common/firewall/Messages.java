package com.wat.melody.common.firewall;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.firewall.messages";

	public static String AccessEx_EMPTY;
	public static String AccessEx_INVALID;

	public static String DirectionEx_EMPTY;
	public static String DirectionEx_INVALID;

	public static String DirectionsEx_EMPTY_DIRECTION;
	public static String DirectionsEx_INVALID_DIRECTION;
	public static String DirectionsEx_EMPTY;

	public static String IcmpCodeEx_EMPTY;
	public static String IcmpCodeEx_NOT_A_NUMBER;
	public static String IcmpCodeEx_NEGATIVE;

	public static String IcmpCodesEx_EMPTY_CODE;
	public static String IcmpCodesEx_INVALID_CODE;
	public static String IcmpCodesEx_EMPTY;

	public static String IcmpTypeEx_EMPTY;
	public static String IcmpTypeEx_NOT_A_NUMBER;
	public static String IcmpTypeEx_NEGATIVE;

	public static String IcmpTypesEx_EMPTY_TYPE;
	public static String IcmpTypesEx_INVALID_TYPE;
	public static String IcmpTypesEx_EMPTY;

	public static String NetworkDeviceNameRefEx_EMPTY;
	public static String NetworkDeviceNameRefEx_INVALID;

	public static String NetworkDeviceNameRefsEx_EMPTY_PART;
	public static String NetworkDeviceNameRefsEx_INVALID_PART;
	public static String NetworkDeviceNameRefsEx_EMPTY;

	public static String NetworkDeviceNameEx_EMPTY;
	public static String NetworkDeviceNameEx_INVALID;

	public static String ProtocolEx_EMPTY;
	public static String ProtocolEx_INVALID;

	public static String ProtocolsEx_EMPTY_PROTOCOL;
	public static String ProtocolsEx_INVALID_PROTOCOL;
	public static String ProtocolsEx_EMPTY;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
