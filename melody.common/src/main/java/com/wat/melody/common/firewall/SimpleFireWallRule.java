package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface SimpleFireWallRule {

	public Protocol getProtocol();

	public Direction getDirection();

	public Access getAccess();

	public Address getFromAddress();

	public Address getToAddress();

}