package com.wat.melody.common.ssh.types;

import com.wat.melody.common.utils.GenericTimeout;
import com.wat.melody.common.utils.Timeout;
import com.wat.melody.common.utils.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ReadTimeout implements Timeout {

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
	public static ReadTimeout parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new ReadTimeout(iTimeout);
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
	public static ReadTimeout parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new ReadTimeout(sTimeout);
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
	public ReadTimeout(long iTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.ReadTimeoutEx_INVALID, iTimeout), Ex);
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
	public ReadTimeout(String sTimeout) throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(sTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.ReadTimeoutEx_INVALID, sTimeout), Ex);
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