package com.wat.melody.common.firewall;

import java.util.Arrays;

import com.wat.melody.common.firewall.exception.IllegalDirectionException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Direction {

	IN("in"), OUT("out");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Direction} object.
	 * </p>
	 * 
	 * @param direction
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Direction} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDirectionException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a valid
	 *             {@link Direction} Enumeration Constant ;</li>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             </ul>
	 */
	public static Direction parseString(String direction)
			throws IllegalDirectionException {
		if (direction == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Direction.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Direction.values()) + " ).");
		}
		if (direction.trim().length() == 0) {
			throw new IllegalDirectionException(Msg.bind(
					Messages.DirectionEx_EMPTY, direction));
		}
		for (Direction c : Direction.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(direction)) {
				return c;
			}
		}
		throw new IllegalDirectionException(Msg.bind(
				Messages.DirectionEx_INVALID, direction,
				Arrays.asList(Direction.values())));
	}

	private final String _value;

	private Direction(String direction) {
		this._value = direction;
	}

	public String getValue() {
		return _value;
	}

}