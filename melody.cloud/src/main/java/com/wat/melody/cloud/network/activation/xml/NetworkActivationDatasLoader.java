package com.wat.melody.cloud.network.activation.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivationDatasLoader {

	protected static Log log = LogFactory
			.getLog(NetworkActivationDatasLoader.class);

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

	protected NetworkActivationProtocol loadMgmtMethod(Element mgmtElmt)
			throws NodeRelatedException {
		return NetworkActivationHelper.getNetworkActivationProtocol(mgmtElmt);
	}

	protected boolean loadNetworkActivationEnabled(Element mgmtElmt) {
		return NetworkActivationHelper.getNetworkActivationEnabled(mgmtElmt);
	}

	protected NetworkActivationTimeout loadNetworkActivationTimeout(
			Element mgmtElmt) throws NodeRelatedException {
		return NetworkActivationHelper.getNetworkActivationTimeout(mgmtElmt);
	}

	protected NetworkDeviceName loadNetworkActivationNetDevName(
			Element instanceNode, Element mgmtElmt) throws NodeRelatedException {
		return NetworkActivationHelper.getNetworkActivationDeviceName(
				instanceNode, mgmtElmt);
	}

	protected Host loadNetworkActivationHost(Element instanceElmt,
			Element mgmtElmt) throws NodeRelatedException {
		Host host = NetworkActivationHelper.getNetworkActivationHost(
				instanceElmt, mgmtElmt);
		if (host == null) {
			Element netElmt = NetworkActivationHelper
					.getNetworkActivationDeviceElement(instanceElmt, mgmtElmt);
			String attr = NetworkActivationHelper
					.getNetworkActivationHostSelector(mgmtElmt);
			throw new NodeRelatedException(netElmt, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, attr));
		}
		return host;
	}

	protected Port loadNetworkActivationPort(Element mgmtElmt)
			throws NodeRelatedException {
		return NetworkActivationHelper.getNetworkActivationPort(mgmtElmt);
	}

	public abstract NetworkActivationDatas load(Element instanceElmt)
			throws NodeRelatedException;

}