package com.wat.melody.common.timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Timeout {

	/**
	 * @return the timeout in seconds.
	 */
	public long getTimeout();

	/**
	 * @return the timeout in milliseconds.
	 */
	public long getTimeoutInMillis();

}