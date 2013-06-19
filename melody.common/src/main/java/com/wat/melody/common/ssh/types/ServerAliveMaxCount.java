package com.wat.melody.common.ssh.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalServerAliveMaxCountException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ServerAliveMaxCount {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> into a {@link ServerAliveMaxCount}
	 * object.
	 * </p>
	 * 
	 * @param maxCount
	 *            is the value to convert.
	 * 
	 * @return a {@link ConnectionTimeout}, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static ServerAliveMaxCount parseInt(int maxCount)
			throws IllegalServerAliveMaxCountException {
		return new ServerAliveMaxCount(maxCount);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> into a {@link ServerAliveMaxCount}
	 * object.
	 * </p>
	 * 
	 * @param maxCount
	 *            is the value to convert.
	 * 
	 * @return a {@link ServerAliveMaxCount}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalServerAliveMaxCountException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static ServerAliveMaxCount parseString(String maxCount)
			throws IllegalServerAliveMaxCountException {
		return new ServerAliveMaxCount(maxCount);
	}

	private int _value;

	/**
	 * <p>
	 * Create a {@link ServerAliveMaxCount} object, which is equal to the given
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param maxCount
	 *            is the value to convert.
	 * 
	 * @throws IllegalServerAliveMaxCountException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public ServerAliveMaxCount(int maxCount)
			throws IllegalServerAliveMaxCountException {
		setValue(maxCount);
	}

	/**
	 * <p>
	 * Create a {@link ServerAliveMaxCount} object, which is equal to the given
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param maxCount
	 *            is the value to convert.
	 * 
	 * @return a {@link ServerAliveMaxCount}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalServerAliveMaxCountException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public ServerAliveMaxCount(String maxCount)
			throws IllegalServerAliveMaxCountException {
		setValue(maxCount);
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
		if (anObject instanceof ServerAliveMaxCount) {
			ServerAliveMaxCount maxcount = (ServerAliveMaxCount) anObject;
			return getValue() == maxcount.getValue();
		}
		return false;
	}

	public int getValue() {
		return _value;
	}

	private int setValue(int maxCount)
			throws IllegalServerAliveMaxCountException {
		if (maxCount < 0) {
			throw new IllegalServerAliveMaxCountException(Msg.bind(
					Messages.ServerAliveMaxCountEx_NEGATIVE, maxCount));
		}
		int previous = getValue();
		_value = maxCount;
		return previous;
	}

	private int setValue(String maxCount)
			throws IllegalServerAliveMaxCountException {
		if (maxCount == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ServerAliveMaxCount.class.getCanonicalName() + ").");
		}
		if (maxCount.trim().length() == 0) {
			throw new IllegalServerAliveMaxCountException(Msg.bind(
					Messages.ServerAliveMaxCountEx_EMPTY, maxCount));
		}
		try {
			return setValue(Integer.parseInt(maxCount));
		} catch (NumberFormatException Ex) {
			throw new IllegalServerAliveMaxCountException(Msg.bind(
					Messages.ServerAliveMaxCountEx_NOT_A_NUMBER, maxCount));
		}
	}

}