package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaId {

	public static final String PATTERN = "\\w+([-._:]\\w+)*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ProtectedAreaId} object.
	 * </p>
	 * 
	 * @param paid
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link ProtectedAreaId} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalProtectedAreaIdException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static ProtectedAreaId parseString(String paid)
			throws IllegalProtectedAreaIdException {
		return new ProtectedAreaId(paid);
	}

	private String _value;

	public ProtectedAreaId(String paid) throws IllegalProtectedAreaIdException {
		setValue(paid);
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
		if (anObject instanceof ProtectedAreaId) {
			ProtectedAreaId d = (ProtectedAreaId) anObject;
			return getValue().equals(d.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String paid) throws IllegalProtectedAreaIdException {
		if (paid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a Protected Area Identifier)");
		}
		if (paid.trim().length() == 0) {
			throw new IllegalProtectedAreaIdException(Msg.bind(
					Messages.ProtectedAreaIdEx_EMPTY, paid));
		}
		if (!paid.matches("^" + PATTERN + "$")) {
			throw new IllegalProtectedAreaIdException(Msg.bind(
					Messages.ProtectedAreaIdEx_INVALID, paid, PATTERN));
		}
		String previous = getValue();
		_value = paid;
		return previous;
	}

}