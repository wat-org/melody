package com.wat.melody.common.telnet.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.types.exception.IllegalConnectionRetryException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConnectionRetry {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> into a {@link ConnectionRetry} object.
	 * </p>
	 * 
	 * @param val
	 *            is the value to convert.
	 * 
	 * @return a {@link ConnectionTimeout}, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalConnectionRetryException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static ConnectionRetry parseInt(int val)
			throws IllegalConnectionRetryException {
		return new ConnectionRetry(val);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> into a {@link ConnectionRetry} object.
	 * </p>
	 * 
	 * @param val
	 *            is the value to convert.
	 * 
	 * @return a {@link ConnectionRetry}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalConnectionRetryException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static ConnectionRetry parseString(String val)
			throws IllegalConnectionRetryException {
		return new ConnectionRetry(val);
	}

	private int _value;

	/**
	 * <p>
	 * Create a {@link ConnectionRetry} object, which is equal to the given
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param val
	 *            is the value to convert.
	 * 
	 * @throws IllegalConnectionRetryException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public ConnectionRetry(int val) throws IllegalConnectionRetryException {
		setValue(val);
	}

	/**
	 * <p>
	 * Create a {@link ConnectionRetry} object, which is equal to the given
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param val
	 *            is the value to convert.
	 * 
	 * @return a {@link ConnectionRetry}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalConnectionRetryException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public ConnectionRetry(String val) throws IllegalConnectionRetryException {
		setValue(val);
	}

	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof ConnectionRetry) {
			ConnectionRetry maxcount = (ConnectionRetry) anObject;
			return getValue() == maxcount.getValue();
		}
		return false;
	}

	public int getValue() {
		return _value;
	}

	private int setValue(int val) throws IllegalConnectionRetryException {
		if (val < 0) {
			throw new IllegalConnectionRetryException(Msg.bind(
					Messages.ConnectionRetryEx_NEGATIVE, val));
		}
		int previous = getValue();
		_value = val;
		return previous;
	}

	private int setValue(String val) throws IllegalConnectionRetryException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ConnectionRetry.class.getCanonicalName() + ").");
		}
		if (val.trim().length() == 0) {
			throw new IllegalConnectionRetryException(Msg.bind(
					Messages.ConnectionRetryEx_EMPTY, val));
		}
		try {
			return setValue(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new IllegalConnectionRetryException(Msg.bind(
					Messages.ConnectionRetryEx_NOT_A_NUMBER, val));
		}
	}

}