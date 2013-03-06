package com.wat.melody.cloud.network;

import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ManagementNetworkEnableTimeout implements Timeout {

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
	public static ManagementNetworkEnableTimeout parseLong(long iTimeout)
			throws IllegalTimeoutException {
		return new ManagementNetworkEnableTimeout(iTimeout);
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
	public static ManagementNetworkEnableTimeout parseString(String sTimeout)
			throws IllegalTimeoutException {
		return new ManagementNetworkEnableTimeout(sTimeout);
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
	public ManagementNetworkEnableTimeout(long iTimeout)
			throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.MgmtNetworkEnableTimeoutEx_INVALID, iTimeout), Ex);
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
	public ManagementNetworkEnableTimeout(String sTimeout)
			throws IllegalTimeoutException {
		try {
			_timeout = new GenericTimeout(sTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new IllegalTimeoutException(Messages.bind(
					Messages.MgmtNetworkEnableTimeoutEx_INVALID, sTimeout), Ex);
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