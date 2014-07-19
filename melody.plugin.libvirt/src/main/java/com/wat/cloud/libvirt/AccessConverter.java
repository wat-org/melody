package com.wat.cloud.libvirt;

import com.wat.melody.common.firewall.Access;

/**
 * <P>
 * Convert an {@link Access} into the corresponding <tt>String</tt>, suitable
 * for libvirt network-filter.
 * 
 * @author Guillaume Cornet
 * 
 */
abstract class AccessConverter {

	protected static String convert(Access access) {
		switch (access) {
		case ALLOW:
			return "accept";
		case DENY:
			return "drop";
		case REJECT:
			return "reject";
		case RETURN:
			return "return";
		default:
			throw new RuntimeException("BUG ! '" + access
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}

}