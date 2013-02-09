package com.wat.melody.plugin.sleep;

import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

public class SleepTimeout implements Timeout {

	public static SleepTimeout DEFAULT_VALUE = createSleepTimeout(1000);

	private static SleepTimeout createSleepTimeout(long iTimeout) {
		try {
			return new SleepTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a sleep timeout. "
					+ "Since this default value is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input int is <= 0.
	 */
	public static SleepTimeout parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new SleepTimeout(iTimeout);
	}

	/**
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input string is <= 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static SleepTimeout parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new SleepTimeout(sTimeout);
	}

	private GenericTimeout _timeout;

	/**
	 * 
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input int is <= 0.
	 */
	public SleepTimeout(long iTimeout) throws IllegalTimeoutException {
		_timeout = new GenericTimeout(iTimeout);
		if (_timeout.getTimeoutInMillis() <= 0) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.SleepTimeoutEx_INVALID, iTimeout));
		}
	}

	/**
	 * @param sTimeout
	 *            in millis
	 * 
	 * @return
	 * 
	 * @throws IllegalTimeoutException
	 *             if input string is <= 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public SleepTimeout(String sTimeout) throws IllegalTimeoutException {
		_timeout = new GenericTimeout(sTimeout);
		if (_timeout.getTimeoutInMillis() <= 0) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.SleepTimeoutEx_INVALID, sTimeout));
		}
	}

	@Override
	public String toString() {
		return _timeout.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		return _timeout.equals(anObject);
	}

	/**
	 * 
	 * @return the timeout in seconds.
	 */
	public long getTimeout() {
		return _timeout.getTimeout();
	}

	/**
	 * 
	 * @return the timeout in milliseconds.
	 */
	public long getTimeoutInMillis() {
		return _timeout.getTimeoutInMillis();
	}

}