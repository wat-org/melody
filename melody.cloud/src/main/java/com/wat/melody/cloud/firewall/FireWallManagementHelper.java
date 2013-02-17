package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FireWallManagementHelper {

	/*
	 * TODO : introduce a <firewall-management selector="//in//fwrule"> in each
	 * instance node. (perfect sample : DiskManagementHelper)
	 */
	public static String findFireWallRulesSelector(Node instanceNode) {
		return "//fwrules//in//fwrule";
	}

}
