package com.wat.melody.common.utils;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Timeout {

	/**
	 * 
	 * @return the timeout in seconds.
	 */
	public long getTimeout();

	/**
	 * 
	 * @return the timeout in milliseconds.
	 */
	public long getTimeoutInMillis();

}
