package com.wat.melody.plugin.sleep;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

public class SleepTimeout implements Timeout<Long> {

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
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link SleepTimeout}, which is equal to the given <tt>long</tt>
	 *         .
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public static SleepTimeout parseLong(long timeout)
			throws IllegalTimeoutException {
		return new SleepTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link SleepTimeout}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>Long</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static SleepTimeout parseString(String timeout)
			throws IllegalTimeoutException {
		return new SleepTimeout(timeout);
	}

	private GenericTimeout _timeout;

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>long</tt> is < 0.
	 */
	public SleepTimeout(long timeout) throws IllegalTimeoutException {
		_timeout = new GenericTimeout(timeout);
		if (_timeout.getTimeoutInMillis() <= 0) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.SleepTimeoutEx_INVALID, timeout));
		}
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
	 *             </ul>
	 */
	public SleepTimeout(String timeout) throws IllegalTimeoutException {
		_timeout = new GenericTimeout(timeout);
		if (_timeout.getTimeoutInMillis() <= 0) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.SleepTimeoutEx_INVALID, timeout));
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
	 * @return the timeout in seconds.
	 */
	public Long getTimeout() {
		return _timeout.getTimeout();
	}

	/**
	 * @return the timeout in milliseconds.
	 */
	public Long getTimeoutInMillis() {
		return _timeout.getTimeoutInMillis();
	}

}