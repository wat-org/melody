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
public class ServerAliveInterval implements Timeout {

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
	public static ServerAliveInterval parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new ServerAliveInterval(iTimeout);
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
	public static ServerAliveInterval parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new ServerAliveInterval(sTimeout);
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
	 *             if input int is < 0.
	 */
	public ServerAliveInterval(long iTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ServerAliveIntervalEx_INVALID, iTimeout), Ex);
		}
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
	public ServerAliveInterval(String sTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(sTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Msg.bind(
					Messages.ServerAliveIntervalEx_INVALID, sTimeout), Ex);
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