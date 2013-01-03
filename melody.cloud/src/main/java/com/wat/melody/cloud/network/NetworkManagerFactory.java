package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.NetworkManagementException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkManagerFactory {

	/*
	 * TODO : remove all reference to Ssh Plug-In.
	 */
	public static NetworkManager createNetworkManager(ITaskContext context,
			Node instanceNode) throws ResourcesDescriptorException,
			NetworkManagementException {
		if (context == null) {
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
			SshPlugInConfiguration sshPlugInConf = null;
			try {
				sshPlugInConf = SshPlugInConfiguration.get(context
						.getProcessorManager());
			} catch (PlugInConfigurationException Ex) {
				throw new NetworkManagementException(Ex);
			}
			return new SshNetworkManager(instanceNode, sshPlugInConf);
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