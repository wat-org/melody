package com.wat.melody.common.firewall;

import java.util.Arrays;

import com.wat.melody.common.firewall.exception.IllegalAccessException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Access {

	ALLOW("allow"), DENY("deny");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link Access} object.
	 * </p>
	 * 
	 * @param access
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link Access} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalAccessException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a valid
	 *             {@link Access} Enumeration Constant ;</li>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             </ul>
	 */
	public static Access parseString(String access)
			throws IllegalAccessException {
		if (access == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Access.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Access.values()) + " ).");
		}
		if (access.trim().length() == 0) {
			throw new IllegalAccessException(Messages.bind(
					Messages.AccessEx_EMPTY, access));
		}
		for (Access c : Access.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(access)) {
				return c;
			}
		}
		throw new IllegalAccessException(Messages.bind(
				Messages.AccessEx_INVALID, access,
				Arrays.asList(Access.values())));
	}

	private final String _value;

	private Access(String access) {
		this._value = access;
	}

	public String getValue() {
		return _value;
	}

}