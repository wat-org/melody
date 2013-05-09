package com.wat.melody.common.bool;

import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.endpoint.exception.IllegalContextRootException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Bool {

	public static final String TRUE_PATTERN = "(?i)\\s*(true|y(es)?|on)\\s*";
	public static final String FALSE_PATTERN = "(?i)\\s*(false|n(o)?|off)\\s*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a <tt>boolean</tt>.
	 * </p>
	 * 
	 * @param contextRoot
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a <tt>boolean</tt>, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalContextRootException
	 *             if the given input <tt>String</tt> is not a valid
	 *             <tt>boolean</tt>.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
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
		throw new IllegalBooleanException(Messages.bind(
				Messages.BooleanEx_INVALID, new Object[] { bool, TRUE_PATTERN,
						FALSE_PATTERN }));
	}

}