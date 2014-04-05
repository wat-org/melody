package com.wat.melody.common.firewall;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ComplexFireWallRule {

	public Protocol getProtocol();

	public FireWallRules decompose();

}