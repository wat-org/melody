package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ManagementNetworkDatasLoader {

	protected static Log log = LogFactory
			.getLog(ManagementNetworkDatasLoader.class);

	/**
	 * XML attribute of the Network Device Management Node, which indicate if
	 * the Network Device Management feature should be used or not for the
	 * related Instance.
	 */
	public static final String ENABLE_ATTR = "enable";

	/**
	 * XML attribute in the SD, which indicate the timeout of the Network Device
	 * Management feature operations.
	 */
	public static final String ENABLE_TIMEOUT_ATTR = "enable-timeout";

	/**
	 * XML attribute of the Network Device Management Node, which indicate the
	 * Network Device Management feature's Management Method to use for the
	 * related Instance.
	 */
	public static final String METHOD_ATTR = "method";

	/**
	 * XML attribute of the Network Device Management Node, which indicate the
	 * Network Device Management feature's Port to use for the related Instance.
	 */
	public static final String PORT_ATTR = "port";

	public ManagementNetworkDatasLoader() {
	}

	protected ManagementNetworkMethod loadMgmtMethod(Element mgmtNode)
			throws NodeRelatedException {
		return NetworkManagementHelper.getManagementNetworkMethod(mgmtNode);
	}

	protected boolean loadMgmtEnable(Element mgmtNode)
			throws NodeRelatedException {
		return NetworkManagementHelper.getManagementNetworkEnable(mgmtNode);
	}

	protected ManagementNetworkEnableTimeout loadMgmtEnableTimeout(
			Element mgmtNode) throws NodeRelatedException {
		return NetworkManagementHelper
				.getManagementNetworkEnableTimeout(mgmtNode);
	}

	protected NetworkDeviceName loadMgmtNetDev(Element instanceNode,
			Element mgmtNode) throws NodeRelatedException {
		return NetworkManagementHelper.getManagementNetworkDeviceName(
				instanceNode, mgmtNode);
	}

	protected Host loadMgmtHost(Element instanceNode, Element mgmtNode)
			throws NodeRelatedException {
		return NetworkManagementHelper.getManagementNetworkHost(instanceNode,
				mgmtNode);
	}

	protected Port loadMgmtPort(Element mgmtNode) throws NodeRelatedException {
		return NetworkManagementHelper.getManagementNetworkPort(mgmtNode);
	}

	public abstract ManagementNetworkDatas load(Element instanceNode)
			throws NodeRelatedException;

}
