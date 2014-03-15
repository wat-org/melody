package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaName {

	public static final String PATTERN = "\\w+([-._:]\\w+)*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ProtectedAreaName} object.
	 * </p>
	 * 
	 * @param paName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link ProtectedAreaName} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalProtectedAreaNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static ProtectedAreaName parseString(String paName)
			throws IllegalProtectedAreaNameException {
		return new ProtectedAreaName(paName);
	}

	private String _value;

	public ProtectedAreaName(String paName)
			throws IllegalProtectedAreaNameException {
		setValue(paName);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	@Override
	public String toString() {
		return getValue();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof ProtectedAreaName) {
			ProtectedAreaName d = (ProtectedAreaName) anObject;
			return getValue().equals(d.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String paName)
			throws IllegalProtectedAreaNameException {
		if (paName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a Protected Area name)");
		}
		if (paName.trim().length() == 0) {
			throw new IllegalProtectedAreaNameException(Msg.bind(
					Messages.ProtectedAreaNameEx_EMPTY, paName));
		}
		if (!paName.matches("^" + PATTERN + "$")) {
			throw new IllegalProtectedAreaNameException(Msg.bind(
					Messages.ProtectedAreaNameEx_INVALID, paName, PATTERN));
		}
		String previous = getValue();
		_value = paName;
		return previous;
	}

}