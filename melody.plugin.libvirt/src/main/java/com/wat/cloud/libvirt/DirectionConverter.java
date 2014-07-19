package com.wat.cloud.libvirt;

import com.wat.melody.common.firewall.Direction;

/**
 * <P>
 * Convert an {@link Direction} into the corresponding <tt>String</tt>, suitable
 * for libvirt network-filter.
 * 
 * @author Guillaume Cornet
 * 
 */
abstract class DirectionConverter {

	protected static String convert(Direction direction) {
		switch (direction) {
		case IN:
			return "in";
		case OUT:
			return "out";
		default:
			throw new RuntimeException("BUG ! '" + direction
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}

}