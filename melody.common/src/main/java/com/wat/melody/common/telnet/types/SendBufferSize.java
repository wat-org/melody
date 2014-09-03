package com.wat.melody.common.telnet.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.types.exception.IllegalSendBufferSizeException;

public class SendBufferSize {

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @return a {@link SendBufferSize}, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalSendBufferSizeException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static SendBufferSize parseInt(int size)
			throws IllegalSendBufferSizeException {
		return new SendBufferSize(size);
	}

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @return a {@link SendBufferSize}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalSendBufferSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public static SendBufferSize parseString(String size)
			throws IllegalSendBufferSizeException {
		return new SendBufferSize(size);
	}

	private int _size;

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @throws IllegalSendBufferSizeException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public SendBufferSize(int size) throws IllegalSendBufferSizeException {
		setSize(size);
	}

	/**
	 * @param size
	 *            represents a buffer size. <tt>0</tt> is accepted, which means
	 *            the default system settings will be used.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalSendBufferSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public SendBufferSize(String size) throws IllegalSendBufferSizeException {
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
		if (anObject instanceof SendBufferSize) {
			SendBufferSize bufferSize = (SendBufferSize) anObject;
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

	private int setSize(int size) throws IllegalSendBufferSizeException {
		if (size < 0) {
			throw new IllegalSendBufferSizeException(Msg.bind(
					Messages.SendBufferSizeEx_NEGATIVE, size));
		}
		int previous = getSize();
		_size = size;
		return previous;
	}

	private int setSize(String size) throws IllegalSendBufferSizeException {
		if (size == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ SendBufferSize.class.getCanonicalName() + ").");
		}
		if (size.trim().length() == 0) {
			throw new IllegalSendBufferSizeException(Msg.bind(
					Messages.SendBufferSizeEx_EMPTY, size));
		}
		try {
			return setSize(Integer.parseInt(size));
		} catch (NumberFormatException Ex) {
			throw new IllegalSendBufferSizeException(Msg.bind(
					Messages.SendBufferSizeEx_NOT_A_NUMBER, size));
		}
	}

}