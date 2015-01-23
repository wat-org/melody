package com.wat.melody.cloud.instance.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.NetworkActivatorConfigurationCallback;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivator;
import com.wat.melody.cloud.network.activation.ssh.xml.SshNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.telnet.TelnetNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.telnet.TelnetNetworkActivator;
import com.wat.melody.cloud.network.activation.telnet.xml.TelnetNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivator;
import com.wat.melody.cloud.network.activation.winrm.xml.WinRmNetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationHelper;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivatorFactoryRelatedToAnInstanceElement {

	private static Logger log = LoggerFactory
			.getLogger(NetworkActivatorFactoryRelatedToAnInstanceElement.class);

	/**
	 * @param configurationCallBack
	 *            is a object which contains Ssh or WinRm configuration.
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return a {@link NetworkActivator}, which have the capacity to activate
	 *         the network of the given Instance.
	 * 
	 * @throws NetworkActivationHostUndefined
	 *             if the Network Activation Host is not defined.
	 * @throws IllegalNetworkActivationDatasException
	 *             if the connection datas are invalid.
	 */
	public static NetworkActivator createNetworkActivator(
			NetworkActivatorConfigurationCallback configurationCallBack,
			Element instanceElmt) throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
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
			log.debug(Msg.bind(Messages.NetworkActivatorMsg_INTRO, DocHelper
					.getNodeLocation(instanceElmt).toFullString()));

			// Find the Activation Protocol
			NetworkActivationProtocol ap;
			try {
				ap = NetworkActivationHelper
						.findNetworkActivationProtocol(instanceElmt);
			} catch (NodeRelatedException Ex) {
				throw new IllegalNetworkActivationDatasException(Ex);
			}
			// no Activation Protocol found => raise an error
			if (ap == null) {
				// throw a precise error message
				Element mgmtElmt = NetworkDevicesHelper
						.findNetworkManagementElement(instanceElmt);
				if (mgmtElmt == null) {
					// if no Network Management Element is defined
					String ne = NetworkDevicesHelper.NETWORK_MGMT_ELEMENT;
					throw new IllegalNetworkActivationDatasException(
							new NodeRelatedException(instanceElmt, Msg.bind(
									Messages.NetMgmtEx_MISSING, ne)));
				}
				// if no Network Activation Protocol is defined
				String attr = NetworkActivationDatasLoader.ACTIVATION_PROTOCOL_ATTR;
				throw new IllegalNetworkActivationDatasException(
						new NodeRelatedException(mgmtElmt, Msg.bind(
								Messages.NetMgmtEx_MISSING_ATTR, attr)));
			}

			NetworkActivator activator;
			NetworkActivationDatas activationDatas;
			switch (ap) {
			case SSH:
				SshNetworkActivationDatas sshDatas = new SshNetworkActivationDatasLoader()
						.load(instanceElmt);
				activator = new SshNetworkActivator(sshDatas,
						configurationCallBack.getSshConfiguration());
				activationDatas = sshDatas;
				break;
			case TELNET:
				TelnetNetworkActivationDatas telnetDatas = new TelnetNetworkActivationDatasLoader()
						.load(instanceElmt);
				activator = new TelnetNetworkActivator(telnetDatas,
						configurationCallBack.getTelnetConfiguration());
				activationDatas = telnetDatas;
				break;
			case WINRM:
				WinRmNetworkActivationDatas winrmDatas = new WinRmNetworkActivationDatasLoader()
						.load(instanceElmt);
				activator = new WinRmNetworkActivator(winrmDatas);
				activationDatas = winrmDatas;
				break;
			default:
				throw new RuntimeException("Unexpected error while branching "
						+ "on an unknown Activation Protocol '" + ap + "'. "
						+ "Source code has certainly been modified and a "
						+ "bug have been introduced.");
			}

			log.debug(Msg.bind(Messages.NetworkActivatorMsg_RESUME,
					activationDatas));

			return activator;
		} catch (NetworkActivationHostUndefined Ex) {
			throw new NetworkActivationHostUndefined(new NodeRelatedException(
					instanceElmt, Messages.NetworkActivatorEx_CREATION_FAILED,
					Ex));
		} catch (IllegalNetworkActivationDatasException Ex) {
			throw new IllegalNetworkActivationDatasException(
					new NodeRelatedException(instanceElmt,
							Messages.NetworkActivatorEx_CREATION_FAILED, Ex));
		}
	}

}