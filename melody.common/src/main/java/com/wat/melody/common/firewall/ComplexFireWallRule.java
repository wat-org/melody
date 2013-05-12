package com.wat.melody.common.firewall;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract interface ComplexFireWallRule {

	public abstract Protocol getProtocol();

	public abstract FireWallRules decompose();

}
