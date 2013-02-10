package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.NetworkManagementException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkManagerFactory {

	public static NetworkManager createNetworkManager(
			NetworkManagerFactoryConfigurationCallback confCB, Node instanceNode)
			throws ResourcesDescriptorException, NetworkManagementException {
		if (confCB == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		ManagementNetworkMethod mm = NetworkManagementHelper
				.findManagementNetworkMethod(instanceNode);

		switch (mm) {
		case SSH:
			return new SshNetworkManager(instanceNode,
					confCB.getSshConfiguration());
		case WINRM:
			return new WinRmNetworkManager(instanceNode);
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "on an unknown management method '" + mm + "'. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}
	}

}