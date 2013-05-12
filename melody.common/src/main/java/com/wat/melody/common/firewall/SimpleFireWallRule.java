package com.wat.melody.common.firewall;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract interface SimpleFireWallRule {

	public abstract Protocol getProtocol();

	public abstract Direction getDirection();

	public abstract Access getAccess();

}
