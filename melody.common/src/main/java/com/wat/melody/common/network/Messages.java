package com.wat.melody.common.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.network.messages";

	public static String HostEx_EMPTY;
	public static String HostEx_INVALID;

	public static String IpAddrEx_EMPTY;
	public static String IpAddrEx_INVALID;

	public static String IpRangeEx_EMPTY;
	public static String IpRangeEx_INVALID;

	public static String IpRangesEx_EMPTY_IP_RANGE;
	public static String IpRangesEx_INVALID_IP_RANGE;

	public static String PortEx_EMPTY;
	public static String PortEx_NAN;
	public static String PortEx_LOW;
	public static String PortEx_HIGH;

	public static String PortRangeEx_EMPTY;
	public static String PortRangeEx_INVALID_START_PART;
	public static String PortRangeEx_INVALID_END_PART;
	public static String PortRangeEx_MISSING_START_TO_PART;
	public static String PortRangeEx_ILLOGIC_RANGE;

	public static String PortRangesEx_EMPTY_PORT_RANGE;
	public static String PortRangesEx_INVALID_PORT_RANGE;
	public static String PortRangesEx_EMPTY;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}