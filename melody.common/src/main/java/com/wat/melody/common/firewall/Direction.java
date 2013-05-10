package com.wat.melody.common.firewall;

import java.util.Arrays;

import com.wat.melody.common.firewall.exception.IllegalDirectionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Direction {

	IN("in"), OUT("out");

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link Direction} object.
	 * </p>
	 * 
	 * @param sDirection
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link Direction} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalDirectionException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Direction} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Direction parseString(String sDirection)
			throws IllegalDirectionException {
		if (sDirection == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Direction.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Direction.values()) + " ).");
		}
		if (sDirection.trim().length() == 0) {
			throw new IllegalDirectionException(Messages.bind(
					Messages.DirectionEx_EMPTY, sDirection));
		}
		for (Direction c : Direction.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sDirection)) {
				return c;
			}
		}
		throw new IllegalDirectionException(Messages.bind(
				Messages.DirectionEx_INVALID, sDirection,
				Arrays.asList(Direction.values())));
	}

	private final String msValue;

	private Direction(String sDirection) {
		this.msValue = sDirection;
	}

	public String getValue() {
		return msValue;
	}

}
