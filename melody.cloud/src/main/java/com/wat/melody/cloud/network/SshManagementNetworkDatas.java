package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshManagementNetworkDatas extends ManagementNetworkDatas {

	public SshManagementNetworkDatas(Node instanceNode)
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
