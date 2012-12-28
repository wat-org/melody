package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class NetworkManagerFactory {

	public static NetworkManager createNetworkManager(ITaskContext context,
			Node instanceNode) throws ResourcesDescriptorException {
		if (context == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		NetworkManagementMethod mm = NetworkManagementHelper
				.findNetworkManagementMethod(instanceNode);

		switch (mm) {
		case SSH:
			return new SshNetworkManager(instanceNode, context);
		case WINRM:
			return new WinRmNetworkManager(instanceNode, context);
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "on an unknown management method '" + mm + "'. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}
	}
}
