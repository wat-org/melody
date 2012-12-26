package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public class NetworkManagerInfos {

	private static Log log = LogFactory.getLog(NetworkManagerInfos.class);

	/*
	 * TODO : add an enable/disable feature, which will indicate that we
	 * want/don't want to use melody management
	 */

	private NetworkManagementMethod moNetworkManagementMethod;
	private Host moHost;
	private Port moPort;

	/**
	 * <p>
	 * Initialize this object with management informations found in the given
	 * {@link Node}.
	 * </p>
	 * 
	 * <p>
	 * <i> * The given node must contains a {@link Common#MGMT_HOST_ATTR} and a
	 * {@link Common#NETWORK_MGMT_PORT_ATTRIBUTE} XML Attributes ; <BR/>
	 * * The {@link Common#NETWORK_MGMT_METHOD_ATTRIBUTE} XML attribute must
	 * contains a {@link NetworkManagementMethod} ; <BR/>
	 * * The {@link Common#NETWORK_MGMT_PORT_ATTRIBUTE} XML attribute must
	 * contains a {@link Port} ; <BR/>
	 * The given node should contains a
	 * {@link Common#NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE} and a
	 * {@link Common#NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE} XML
	 * Attributes ; <BR/>
	 * * The {@link Common#NETWORK_MGMT_INTERFACE_NODE_SELECTOR_ATTRIBUTE} XML
	 * attribute must contains an XPath expression which select the Management
	 * Network Interface Node ; <BR/>
	 * * The {@link Common#NETWORK_MGMT_INTERFACE_ATTR_SELECTOR_ATTRIBUTE} XML
	 * attribute must contains the name of the attribute of the Management
	 * Network Interface Node which contains the {@link Host} ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param instanceNode
	 * 
	 * @throws ResourcesDescriptorException
	 */
	public NetworkManagerInfos(Node instanceNode)
			throws ResourcesDescriptorException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		log.debug(Messages.bind(Messages.MgmtMsg_INTRO,
				Doc.getNodeLocation(instanceNode).toFullString()));
		try {
			Node mgmtNode = NetworkManagementHelper
					.findNetworkManagementNode(instanceNode);
			setManagementMethod(NetworkManagementHelper
					.getNetworkManagementMethod(mgmtNode));
			setHost(NetworkManagementHelper.getNetworkManagementHost(mgmtNode,
					instanceNode));
			setPort(NetworkManagementHelper.getNetworkManagementPort(mgmtNode));
			log.info(Messages.bind(Messages.MgmtMsg_RESUME, this));
		} catch (ResourcesDescriptorException Ex) {
			log.warn(Messages.bind(Messages.MgmtMsg_FAILED, Doc
					.getNodeLocation(instanceNode).toFullString()));
			throw Ex;
		}
	}

	@Override
	public String toString() {
		return "{ method:" + getNetworkManagementMethod() + ", host:"
				+ getHost() + ", port:" + getPort() + " }";
	}

	public NetworkManagementMethod getNetworkManagementMethod() {
		return moNetworkManagementMethod;
	}

	private NetworkManagementMethod setManagementMethod(
			NetworkManagementMethod mm) {
		if (mm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkManagementMethod.class.getCanonicalName() + ".");
		}
		NetworkManagementMethod previous = getNetworkManagementMethod();
		moNetworkManagementMethod = mm;
		return previous;
	}

	public Host getHost() {
		return moHost;
	}

	private Host setHost(Host h) {
		if (h == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getHost();
		moHost = h;
		return previous;
	}

	public Port getPort() {
		return moPort;
	}

	private Port setPort(Port p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getPort();
		moPort = p;
		return previous;
	}

}
