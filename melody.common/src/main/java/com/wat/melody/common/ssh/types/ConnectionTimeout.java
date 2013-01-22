package com.wat.melody.common.ssh.types;

import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConnectionTimeout implements Timeout {

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
	public static ConnectionTimeout parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new ConnectionTimeout(iTimeout);
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
	public static ConnectionTimeout parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new ConnectionTimeout(sTimeout);
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
	public ConnectionTimeout(long iTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.CnxTimeoutEx_INVALID, iTimeout), Ex);
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
	public ConnectionTimeout(String sTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(sTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.CnxTimeoutEx_INVALID, sTimeout), Ex);
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