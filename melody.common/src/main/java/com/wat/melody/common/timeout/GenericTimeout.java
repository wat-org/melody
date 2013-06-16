package com.wat.melody.common.timeout;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GenericTimeout implements Timeout {

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link GenericTimeout}, which is equal to the given
	 *         <tt>long</tt>.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public static GenericTimeout parseLong(long timeout)
			throws IllegalTimeoutException {
		return new GenericTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link GenericTimeout}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Long</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public static GenericTimeout parseString(String timeout)
			throws IllegalTimeoutException {
		return new GenericTimeout(timeout);
	}

	private long _timeout;

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public GenericTimeout(long timeout) throws IllegalTimeoutException {
		setTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Long</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 */
	public GenericTimeout(String timeout) throws IllegalTimeoutException {
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
		if (anObject instanceof Timeout) {
			Timeout timeout = (Timeout) anObject;
			return getTimeout() == timeout.getTimeout();
		}
		return false;
	}

	/**
	 * @return the timeout, in seconds.
	 */
	public long getTimeout() {
		return _timeout / 1000;
	}

	/**
	 * @return the timeout, in milliseconds.
	 */
	public long getTimeoutInMillis() {
		return _timeout;
	}

	private long setTimeout(long timeout) throws IllegalTimeoutException {
		if (timeout < 0) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.TimeoutEx_NEGATIVE, timeout));
		}
		long previous = getTimeout();
		_timeout = timeout;
		return previous;
	}

	private long setTimeout(String timeout) throws IllegalTimeoutException {
		if (timeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Timeout.class.getCanonicalName() + ").");
		}
		if (timeout.trim().length() == 0) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.TimeoutEx_EMPTY, timeout));
		}
		try {
			return setTimeout(Long.parseLong(timeout));
		} catch (NumberFormatException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.TimeoutEx_NOT_A_NUMBER, timeout));
		}
	}

	/**
	 * @param ratio
	 *            represents a ratio.
	 * 
	 * @return a {@link GenericTimeout} object, which is equal to this object's
	 *         timeout value multiplied by the given ratio.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>float</tt> is negative or 0.
	 */
	public GenericTimeout factor(float ratio) {
		if (ratio <= 0) {
			throw new IllegalArgumentException(ratio + ": Not accepted. "
					+ "Must be postive.");
		}
		try {
			return new GenericTimeout((long) (getTimeoutInMillis() * ratio));
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a " + GenericTimeout.class.getCanonicalName() + " to "
					+ (long) (getTimeoutInMillis() * ratio) + ". "
					+ "Because the given timeout is valid, such error cannot "
					+ "happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

}