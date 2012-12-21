package com.wat.melody.cloud.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.xpathextensions.common.ManagementInterfaceHelper;
import com.wat.melody.xpathextensions.common.ManagementMethod;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public class ManagementInfos {

	private static Log log = LogFactory.getLog(ManagementInfos.class);

	private ManagementMethod moManagementMethod;
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
	 * {@link Common#MGMT_PORT_ATTR} XML Attributes ; <BR/>
	 * * The {@link Common#MGMT_METHOD_ATTR} XML attribute must contains a
	 * {@link ManagementMethod} ; <BR/>
	 * * The {@link Common#MGMT_PORT_ATTR} XML attribute must contains a
	 * {@link Port} ; <BR/>
	 * The given node should contains a
	 * {@link Common#MGMT_NETWORK_INTERFACE_NODE_SELECTOR} and a
	 * {@link Common#MGMT_NETWORK_INTERFACE_ATTR_SELECTOR} XML Attributes ; <BR/>
	 * * The {@link Common#MGMT_NETWORK_INTERFACE_NODE_SELECTOR} XML attribute must
	 * contains an XPath expression which select the Management Network
	 * Interface Node ; <BR/>
	 * * The {@link Common#MGMT_NETWORK_INTERFACE_ATTR_SELECTOR} XML attribute must
	 * contains the name of the attribute of the Management Network Interface
	 * Node which contains the {@link Host} ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param context
	 * @param instanceNode
	 * 
	 * @throws ResourcesDescriptorException
	 */
	public ManagementInfos(ITaskContext context, Node instanceNode)
			throws ResourcesDescriptorException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		log.debug(Messages.bind(Messages.MgmtMsg_INTRO,
				context.getProcessorManager().getResourcesDescriptor()
						.getLocation(instanceNode).toFullString()));
		Node mgmtNode = ManagementInterfaceHelper.findMgmtNode(instanceNode);
		setManagementMethod(ManagementInterfaceHelper
				.getManagementMethod(mgmtNode));
		setHost(ManagementInterfaceHelper.getManagementNetworkInterfaceHost(
				mgmtNode, instanceNode));
		setPort(ManagementInterfaceHelper.getManagementPort(mgmtNode));
		log.info(Messages.bind(Messages.MgmtMsg_RESUME, new Object[] {
				getManagementMethod(), getHost(), getPort() }));
	}

	public ManagementMethod getManagementMethod() {
		return moManagementMethod;
	}

	private ManagementMethod setManagementMethod(ManagementMethod mm) {
		if (mm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ManagementMethod.class.getCanonicalName() + ".");
		}
		ManagementMethod previous = getManagementMethod();
		moManagementMethod = mm;
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
