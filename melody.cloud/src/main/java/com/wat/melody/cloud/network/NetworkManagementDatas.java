package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;

public abstract class NetworkManagementDatas {

	private static Log log = LogFactory.getLog(SshNetworkManagementDatas.class);

	private Host moHost;
	private Port moPort;

	/**
	 * <p>
	 * Initialize this object with Network Management datas found in the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Network Management {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Network Management Device Node Selector is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Network
	 *             Management Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 * @throws ResourcesDescriptorException
	 *             if no {@link NetworkManagementHelper#NETWORK_MGMT_PORT_ATTR}
	 *             can be found in the Instance's Network Management
	 *             {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the
	 *             {@link NetworkManagementHelper#NETWORK_MGMT_PORT_ATTR} found
	 *             in the Instance's Network Management {@link Node} is not a
	 *             valid {@link Port}.
	 */
	public NetworkManagementDatas(Node instanceNode)
			throws ResourcesDescriptorException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		log.debug(Messages.bind(Messages.NetMgmtMsg_INTRO,
				Doc.getNodeLocation(instanceNode).toFullString()));
		try {
			Node mgmtNode = NetworkManagementHelper
					.findNetworkManagementNode(instanceNode);
			setHost(NetworkManagementHelper.getManagementNetworkHost(
					instanceNode, mgmtNode));
			setPort(NetworkManagementHelper.getManagementNetworkPort(mgmtNode));
			log.info(Messages.bind(Messages.NetMgmtMsg_RESUME, this));
		} catch (ResourcesDescriptorException Ex) {
			log.warn(Messages.bind(Messages.NetMgmtMsg_FAILED, Doc
					.getNodeLocation(instanceNode).toFullString()));
			throw Ex;
		}
	}

	@Override
	public String toString() {
		return "{ method:" + getNetworkManagementMethod() + ", host:"
				+ getHost() + ", port:" + getPort() + " }";
	}

	abstract public NetworkManagementMethod getNetworkManagementMethod();

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
