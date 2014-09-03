package com.wat.melody.common.telnet.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.types.exception.IllegalSoLingerException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SoLinger {

	/**
	 * @param timeout
	 *            represents a SO_LINGER timeout in seconds. <tt>-1</tt> is
	 *            accepted, which represents a disabled SO_LINGER.
	 * 
	 * @return a {@link SoLinger}, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalSoLingerException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public static SoLinger parseInt(int timeout)
			throws IllegalSoLingerException {
		return new SoLinger(timeout);
	}

	/**
	 * @param timeout
	 *            represents a SO_LINGER timeout in seconds. <tt>-1</tt> is
	 *            accepted, which represents a disabled SO_LINGER.
	 * 
	 * @return a {@link SoLinger}, which is equal to the given <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalSoLingerException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < -1 ;</li>
	 *             </ul>
	 */
	public static SoLinger parseString(String timeout)
			throws IllegalSoLingerException {
		return new SoLinger(timeout);
	}

	private int _timeout;

	/**
	 * @param timeout
	 *            represents a SO_LINGER timeout in seconds. <tt>-1</tt> is
	 *            accepted, which represents a disabled SO_LINGER.
	 * 
	 * @throws IllegalSoLingerException
	 *             if the given <tt>int</tt> is < -1.
	 */
	public SoLinger(int timeout) throws IllegalSoLingerException {
		setTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a SO_LINGER timeout in seconds. <tt>-1</tt> is
	 *            accepted, which represents a disabled SO_LINGER.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalSoLingerException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < -1 ;</li>
	 *             </ul>
	 */
	public SoLinger(String timeout) throws IllegalSoLingerException {
		setTimeout(timeout);
	}

	@Override
	public int hashCode() {
		return (int) getTimeout();
	}

	@Override
	public String toString() {
		return String.valueOf(_timeout);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof SoLinger) {
			SoLinger timeout = (SoLinger) anObject;
			return getTimeout() == timeout.getTimeout();
		}
		return false;
	}

	/**
	 * @return the SO_LINGER timeout, in seconds, or <tt>-1</tt>, if SO_LINGER
	 *         is disabled.
	 */
	public int getTimeout() {
		return _timeout;
	}

	public boolean isEnabled() {
		return getTimeout() != -1;
	}

	private int setTimeout(int timeout) throws IllegalSoLingerException {
		if (timeout < -1) {
			throw new IllegalSoLingerException(Msg.bind(
					Messages.SoLingerEx_NEGATIVE, timeout));
		}
		int previous = getTimeout();
		_timeout = timeout;
		return previous;
	}

	private int setTimeout(String timeout) throws IllegalSoLingerException {
		if (timeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ SoLinger.class.getCanonicalName() + ").");
		}
		if (timeout.trim().length() == 0) {
			throw new IllegalSoLingerException(Msg.bind(
					Messages.SoLingerEx_EMPTY, timeout));
		}
		try {
			return setTimeout(Integer.parseInt(timeout));
		} catch (NumberFormatException Ex) {
			throw new IllegalSoLingerException(Msg.bind(
					Messages.SoLingerEx_NOT_A_NUMBER, timeout));
		}
	}

}