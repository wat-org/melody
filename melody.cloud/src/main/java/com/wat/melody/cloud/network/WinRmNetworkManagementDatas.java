package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;

public class WinRmNetworkManagementDatas extends NetworkManagementDatas {

	public WinRmNetworkManagementDatas(Node instanceNode)
			throws ResourcesDescriptorException {
		super(instanceNode);
	}

	@Override
	public String toString() {
		return "{ method:" + getNetworkManagementMethod() + ", host:"
				+ getHost() + ", port:" + getPort() + " }";
	}

	public NetworkManagementMethod getNetworkManagementMethod() {
		return NetworkManagementMethod.WINRM;
	}

}
