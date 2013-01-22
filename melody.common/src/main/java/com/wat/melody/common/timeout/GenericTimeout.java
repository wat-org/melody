package com.wat.melody.common.timeout;

import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GenericTimeout implements Timeout {

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input int is < 0.
	 */
	public static GenericTimeout parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new GenericTimeout(iTimeout);
	}

	/**
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static GenericTimeout parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new GenericTimeout(sTimeout);
	}

	private long _timeout;

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input int is < 0.
	 */
	public GenericTimeout(long iTimeout) throws IllegalTimeoutException {
		setTimeout(iTimeout);
	}

	/**
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public GenericTimeout(String sTimeout) throws IllegalTimeoutException {
		setTimeout(sTimeout);
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
	 * 
	 * @return the timeout in seconds.
	 */
	public long getTimeout() {
		return _timeout / 1000;
	}

	/**
	 * 
	 * @return the timeout in milliseconds.
	 */
	public long getTimeoutInMillis() {
		return _timeout;
	}

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input int is < 0.
	 */
	private long setTimeout(long iTimeout) throws IllegalTimeoutException {
		if (iTimeout < 0) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.TimeoutEx_NEGATIVE, iTimeout));
		}
		long previous = getTimeout();
		_timeout = iTimeout;
		return previous;
	}

	/**
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	private long setTimeout(String sTimeout) throws IllegalTimeoutException {
		if (sTimeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Timeout.class.getCanonicalName() + ").");
		}
		if (sTimeout.trim().length() == 0) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.TimeoutEx_EMPTY, sTimeout));
		}
		try {
			return setTimeout(Long.parseLong(sTimeout));
		} catch (NumberFormatException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.TimeoutEx_NOT_A_NUMBER, sTimeout));
		}
	}

}