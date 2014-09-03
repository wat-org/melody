package com.wat.melody.common.telnet.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.types.exception.IllegalReceiveBufferSizeException;

public class ReceiveBufferSize {

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @return a {@link ReceiveBufferSize}, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalReceiveBufferSizeException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static ReceiveBufferSize parseInt(int size)
			throws IllegalReceiveBufferSizeException {
		return new ReceiveBufferSize(size);
	}

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @return a {@link ReceiveBufferSize}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalReceiveBufferSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public static ReceiveBufferSize parseString(String size)
			throws IllegalReceiveBufferSizeException {
		return new ReceiveBufferSize(size);
	}

	private int _size;

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @throws IllegalReceiveBufferSizeException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public ReceiveBufferSize(int size) throws IllegalReceiveBufferSizeException {
		setSize(size);
	}

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalReceiveBufferSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public ReceiveBufferSize(String size)
			throws IllegalReceiveBufferSizeException {
		setSize(size);
	}

	@Override
	public int hashCode() {
		return getSize();
	}

	@Override
	public String toString() {
		return String.valueOf(_size);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof ReceiveBufferSize) {
			ReceiveBufferSize bufferSize = (ReceiveBufferSize) anObject;
			return getSize() == bufferSize.getSize();
		}
		return false;
	}

	/**
	 * @return the buffer size, or <tt>0</tt>, if the default system settings
	 *         should be used.
	 */
	public int getSize() {
		return _size;
	}

	public boolean isDefined() {
		return getSize() != -0;
	}

	private int setSize(int size) throws IllegalReceiveBufferSizeException {
		if (size < 0) {
			throw new IllegalReceiveBufferSizeException(Msg.bind(
					Messages.ReceiveBufferSizeEx_NEGATIVE, size));
		}
		int previous = getSize();
		_size = size;
		return previous;
	}

	private int setSize(String size) throws IllegalReceiveBufferSizeException {
		if (size == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ReceiveBufferSize.class.getCanonicalName() + ").");
		}
		if (size.trim().length() == 0) {
			throw new IllegalReceiveBufferSizeException(Msg.bind(
					Messages.ReceiveBufferSizeEx_EMPTY, size));
		}
		try {
			return setSize(Integer.parseInt(size));
		} catch (NumberFormatException Ex) {
			throw new IllegalReceiveBufferSizeException(Msg.bind(
					Messages.ReceiveBufferSizeEx_NOT_A_NUMBER, size));
		}
	}

}