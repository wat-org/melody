package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpathextensions.common.ManagementNetworkMethod;

public class SshNetworkManagementDatas extends ManagementNetworkDatas {

	public SshNetworkManagementDatas(Node instanceNode)
			throws ResourcesDescriptorException {
		super(instanceNode);
	}

	@Override
	public String toString() {
		return "{ method:" + getManagementNetworkMethod() + ", host:"
				+ getHost() + ", port:" + getPort() + " }";
	}

	public ManagementNetworkMethod getManagementNetworkMethod() {
		return ManagementNetworkMethod.SSH;
	}

}
