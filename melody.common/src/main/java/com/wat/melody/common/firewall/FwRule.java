package com.wat.melody.common.firewall;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract interface FwRule {

	public abstract Protocol getProtocol();

	public abstract FwRulesDecomposed decompose();

}
