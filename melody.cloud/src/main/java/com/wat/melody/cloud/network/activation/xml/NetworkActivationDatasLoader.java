package com.wat.melody.cloud.network.activation.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivationDatasLoader {

	protected static Logger log = LoggerFactory
			.getLogger(NetworkActivationDatasLoader.class);

	/**
	 * XML attribute of the Network Management Element, which indicate if the
	 * Network Activation feature should be used or not for the related
	 * Instance.
	 */
	public static final String ACTIVATION_ENABLED_ATTR = "activation-enabled";

	/**
	 * XML attribute of the Network Management Element, which specifies the
	 * Activation Timeout used for the related Instance Network Activation
	 * operations.
	 */
	public static final String ACTIVATION_TIMEOUT_ATTR = "activation-timeout";

	/**
	 * XML attribute of the Network Management Element, which specifies the
	 * Activation Protocol used for the related Instance Network Activation
	 * operations.
	 */
	public static final String ACTIVATION_PROTOCOL_ATTR = "activation-protocol";

	/**
	 * XML attribute of the Network Management Element, which specifies the
	 * Activation Port used for the related Instance Network Activation
	 * operations.
	 */
	public static final String ACTIVATION_PORT_ATTR = "activation-port";

	public NetworkActivationDatasLoader() {
	}

	protected NetworkActivationProtocol loadNetworkActivationProtocol(
			Element mgmtElmt) throws IllegalNetworkActivationDatasException {
		try {
			return NetworkActivationHelper
					.getNetworkActivationProtocol(mgmtElmt);
		} catch (NodeRelatedException Ex) {
			throw new IllegalNetworkActivationDatasException(Ex);
		}
	}

	protected boolean loadNetworkActivationEnabled(Element mgmtElmt) {
		return NetworkActivationHelper.getNetworkActivationEnabled(mgmtElmt);
	}

	protected NetworkActivationTimeout loadNetworkActivationTimeout(
			Element mgmtElmt) throws IllegalNetworkActivationDatasException {
		try {
			return NetworkActivationHelper
					.getNetworkActivationTimeout(mgmtElmt);
		} catch (NodeRelatedException Ex) {
			throw new IllegalNetworkActivationDatasException(Ex);
		}
	}

	protected NetworkDeviceName loadNetworkActivationNetDevName(
			Element instanceNode, Element mgmtElmt)
			throws IllegalNetworkActivationDatasException {
		try {
			return NetworkActivationHelper.getNetworkActivationDeviceName(
					instanceNode, mgmtElmt);
		} catch (NodeRelatedException Ex) {
			throw new IllegalNetworkActivationDatasException(Ex);
		}
	}

	protected Host loadNetworkActivationHost(Element instanceElmt,
			Element mgmtElmt) throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
		try {
			Host host = NetworkActivationHelper.getNetworkActivationHost(
					instanceElmt, mgmtElmt);
			if (host == null) {
				Element netElmt = NetworkActivationHelper
						.getNetworkActivationDeviceElement(instanceElmt,
								mgmtElmt);
				String attr = NetworkActivationHelper
						.getNetworkActivationHostSelector(mgmtElmt);
				throw new NetworkActivationHostUndefined(
						new NodeRelatedException(netElmt, Msg.bind(
								Messages.NetMgmtEx_MISSING_ATTR, attr)));
			}
			return host;
		} catch (NodeRelatedException Ex) {
			throw new IllegalNetworkActivationDatasException(Ex);
		}
	}

	protected Port loadNetworkActivationPort(Element mgmtElmt)
			throws IllegalNetworkActivationDatasException {
		try {
			return NetworkActivationHelper.getNetworkActivationPort(mgmtElmt);
		} catch (NodeRelatedException Ex) {
			throw new IllegalNetworkActivationDatasException(Ex);
		}
	}

	public abstract NetworkActivationDatas load(Element instanceElmt)
			throws IllegalNetworkActivationDatasException;

}