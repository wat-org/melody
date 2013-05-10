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
	 * Convert the given <code>String</code> to an {@link Access} object.
	 * </p>
	 * 
	 * @param sAccess
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link Access} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalAccessException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Access} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Access parseString(String sAccess)
			throws IllegalAccessException {
		if (sAccess == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Access.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Access.values()) + " ).");
		}
		if (sAccess.trim().length() == 0) {
			throw new IllegalAccessException(Messages.bind(
					Messages.AccessEx_EMPTY, sAccess));
		}
		for (Access c : Access.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sAccess)) {
				return c;
			}
		}
		throw new IllegalAccessException(Messages.bind(
				Messages.AccessEx_INVALID, sAccess,
				Arrays.asList(Access.values())));
	}

	private final String msValue;

	private Access(String sAccess) {
		this.msValue = sAccess;
	}

	public String getValue() {
		return msValue;
	}

}
