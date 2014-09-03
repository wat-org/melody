package com.wat.melody.common.timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Timeout<T extends Number> {

	/**
	 * @return the timeout in seconds.
	 */
	public T getTimeout();

	/**
	 * @return the timeout in milliseconds.
	 */
	public T getTimeoutInMillis();

}