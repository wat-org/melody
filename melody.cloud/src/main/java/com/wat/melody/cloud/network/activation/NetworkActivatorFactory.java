package com.wat.melody.cloud.network.activation;

import org.w3c.dom.Element;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivator;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivator;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivatorFactory {

	/**
	 * @param configurationCallBack
	 *            is a object which contains Ssh or WinRm configuration.
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return a {@link NetworkActivator}, which have the capacity to activate
	 *         the network of the given Instance, or <tt>null</tt> if any
	 *         problem occurred during the creation of this object.
	 */
	public static NetworkActivator createNetworkManager(
			NetworkActivatorConfigurationCallback configurationCallBack,
			Element instanceElmt) {
		if (configurationCallBack == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		try {
			NetworkActivationProtocol ac = NetworkActivationHelper
					.findNetworkActivationProtocol(instanceElmt);
			switch (ac) {
			case SSH:
				return new SshNetworkActivator(
						new SshNetworkActivationDatasLoader()
								.load(instanceElmt),
						configurationCallBack.getSshConfiguration());
			case WINRM:
				return new WinRmNetworkActivator(
						new WinRmNetworkActivationDatasLoader()
								.load(instanceElmt));
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown Activation Protocol '" + ac + "'. "
						+ "Source code has certainly been modified and a "
						+ "bug have been introduced.");
			}
		} catch (NodeRelatedException Ex) {
			return null;
		}
	}

}