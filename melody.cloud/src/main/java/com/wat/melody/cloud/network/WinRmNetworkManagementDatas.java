package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpathextensions.common.ManagementNetworkMethod;

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

	public ManagementNetworkMethod getNetworkManagementMethod() {
		return ManagementNetworkMethod.WINRM;
	}

}
