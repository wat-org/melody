package com.wat.melody.common.ssh.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ReadTimeout implements Timeout<Integer> {

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ReadTimeout}, which is equal to the given <tt>int</tt>.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static ReadTimeout parseInt(int timeout)
			throws IllegalTimeoutException {
		return new ReadTimeout(timeout);
	}

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ReadTimeout}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             is the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTimeoutException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a parse-able
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static ReadTimeout parseString(String timeout)
			throws IllegalTimeoutException {
		return new ReadTimeout(timeout);
	}

	private GenericTimeout _timeout;

	/**
	 * @param timeout
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public ReadTimeout(int timeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ReadTimeoutEx_INVALID, timeout), Ex);
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
	 *             <tt>int</tt> ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public ReadTimeout(String timeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ReadTimeoutEx_INVALID, timeout), Ex);
		}
	}

	@Override
	public int hashCode() {
		return _timeout.hashCode();
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
	 * @return the timeout, in seconds.
	 */
	@Override
	public Integer getTimeout() {
		return _timeout.getTimeout().intValue();
	}

	/**
	 * @return the timeout, in milliseconds.
	 */
	@Override
	public Integer getTimeoutInMillis() {
		return _timeout.getTimeoutInMillis().intValue();
	}

}