package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkManagerFactory {

	public static NetworkManager createNetworkManager(
			NetworkManagerFactoryConfigurationCallback confCB, Node instanceNode) {
		if (confCB == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		try {
			ManagementNetworkMethod mm = NetworkManagementHelper
					.findManagementNetworkMethod(instanceNode);
			switch (mm) {
			case SSH:
				return new SshNetworkManager(
						new SshManagementNetworkDatasLoader()
								.load(instanceNode),
						confCB.getSshConfiguration());
			case WINRM:
				return new WinRmNetworkManager(
						new WinRmManagementNetworkDatasLoader()
								.load(instanceNode));
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown management method '" + mm + "'. "
						+ "Source code has certainly been modified and a "
						+ "bug have been introduced.");
			}
		} catch (ResourcesDescriptorException Ex) {
			return null;
		}
	}

}