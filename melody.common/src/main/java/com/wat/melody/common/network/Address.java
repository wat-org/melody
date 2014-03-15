package com.wat.melody.common.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Address {
	/*
	 * TODO : should be a generic, like Address<T>, with a method public T
	 * getAddress(). This will allow a complete flexibility.
	 */

	public String getAddressAsString();

}