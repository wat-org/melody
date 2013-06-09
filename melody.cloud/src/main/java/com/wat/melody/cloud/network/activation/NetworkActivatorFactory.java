package com.wat.melody.cloud.network.activation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivator;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivator;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivatorFactory {

	private static Log log = LogFactory.getLog(NetworkActivatorFactory.class);

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
	public static NetworkActivator createNetworkActivator(
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
			log.debug(Messages.bind(Messages.NetworkActivatorMsg_INTRO,
					DocHelper.getNodeLocation(instanceElmt).toFullString()));

			NetworkActivationProtocol ap = NetworkActivationHelper
					.findNetworkActivationProtocol(instanceElmt);
			NetworkActivator activator;
			switch (ap) {
			case SSH:
				activator = new SshNetworkActivator(
						new SshNetworkActivationDatasLoader()
								.load(instanceElmt),
						configurationCallBack.getSshConfiguration());
				break;
			case WINRM:
				activator = new WinRmNetworkActivator(
						new WinRmNetworkActivationDatasLoader()
								.load(instanceElmt));
				break;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown Activation Protocol '" + ap + "'. "
						+ "Source code has certainly been modified and a "
						+ "bug have been introduced.");
			}
			log.debug(Messages.bind(Messages.NetworkActivatorMsg_RESUME,
					activator.getDatas()));
			return activator;
		} catch (NodeRelatedException Ex) {
			log.debug(new MelodyException(Messages.bind(
					Messages.NetworkActivatorMsg_CREATION_FAILED, DocHelper
							.getNodeLocation(instanceElmt).toFullString()), Ex)
					.toString());
			return null;
		}
	}

}