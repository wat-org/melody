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
public class ServerAliveInterval implements Timeout<Integer> {

	/**
	 * @param aliveInterval
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ServerAliveInterval}, which is equal to the given
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public static ServerAliveInterval parseInt(int aliveInterval)
			throws IllegalTimeoutException {
		return new ServerAliveInterval(aliveInterval);
	}

	/**
	 * @param aliveInterval
	 *            represents a timeout value in millis.
	 * 
	 * @return a {@link ServerAliveInterval}, which is equal to the given
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
	public static ServerAliveInterval parseString(String aliveInterval)
			throws IllegalTimeoutException {
		return new ServerAliveInterval(aliveInterval);
	}

	private GenericTimeout _timeout;

	/**
	 * @param aliveInterval
	 *            represents a timeout value in millis.
	 * 
	 * @throws IllegalTimeoutException
	 *             if the given <tt>int</tt> is < 0.
	 */
	public ServerAliveInterval(int aliveInterval)
			throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(aliveInterval);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ServerAliveIntervalEx_INVALID, aliveInterval), Ex);
		}
	}

	/**
	 * @param aliveInterval
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
	public ServerAliveInterval(String aliveInterval)
			throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(aliveInterval);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ServerAliveIntervalEx_INVALID, aliveInterval), Ex);
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
	public Integer getTimeout() {
		return _timeout.getTimeout().intValue();
	}

	/**
	 * @return the timeout, in milliseconds.
	 */
	public Integer getTimeoutInMillis() {
		return _timeout.getTimeoutInMillis().intValue();
	}

}