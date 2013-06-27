package com.wat.melody.common.bool;

import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Bool {

	public static final String TRUE_PATTERN = "(?i)\\s*(1|true|y(es)?|on)\\s*";
	public static final String FALSE_PATTERN = "(?i)\\s*(0|false|n(o)?|off)\\s*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a <tt>boolean</tt>.
	 * </p>
	 * 
	 * @param bool
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a <tt>boolean</tt>, which is equal to the given <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalBooleanException
	 *             if the given <tt>String</tt> neither matches the pattern
	 *             {@link #TRUE_PATTERN} nor the pattern {@link #FALSE_PATTERN}.
	 */
	public static boolean parseString(String bool)
			throws IllegalBooleanException {
		if (bool == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a boolean).");
		}
		if (bool.matches("^" + TRUE_PATTERN + "$")) {
			return true;
		} else if (bool.matches("^" + FALSE_PATTERN + "$")) {
			return false;
		}
		throw new IllegalBooleanException(Msg.bind(Messages.BooleanEx_INVALID,
				bool, TRUE_PATTERN, FALSE_PATTERN));
	}

}