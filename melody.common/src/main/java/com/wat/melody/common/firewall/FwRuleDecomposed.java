package com.wat.melody.common.firewall;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract interface FwRuleDecomposed {

	public abstract Protocol getProtocol();

	public abstract Interface getInterface();

	public abstract Direction getDirection();

	public abstract Access getAccess();

}
