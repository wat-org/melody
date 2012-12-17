package com.wat.melody.cloud.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.management.exception.IllegalManagementMethodException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.xpathextensions.common.ManagementInterfaceHelper;
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
	 * {@link Common#MGMT_NETWORK_INTERFACE_SELECTOR} and a
	 * {@link Common#MGMT_NETWORK_INTERFACE_ATTR} XML Attributes ; <BR/>
	 * * The {@link Common#MGMT_NETWORK_INTERFACE_SELECTOR} XML attribute must
	 * contains an XPath expression which select the Management Network
	 * Interface Node ; <BR/>
	 * * The {@link Common#MGMT_NETWORK_INTERFACE_ATTR} XML attribute must
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
		loadMethod(mgmtNode);
		loadHost(mgmtNode, instanceNode);
		loadPort(mgmtNode);
		log.info(Messages.bind(Messages.MgmtMsg_RESUME, new Object[] {
				getManagementMethod(), getHost(), getPort() }));
	}

	private void loadMethod(Node mgmtNode) throws ResourcesDescriptorException {
		String sMethod = null;
		try {
			sMethod = mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_METHOD_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_MISSING_ATTR, Common.MGMT_METHOD_ATTR));
		}
		try {
			setManagementMethod(sMethod);
		} catch (IllegalManagementMethodException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, Common.MGMT_METHOD_ATTR), Ex);
		}
	}

	private void loadHost(Node mgmtNode, Node instanceNode)
			throws ResourcesDescriptorException {
		String sHost = ManagementInterfaceHelper
				.getManagementNetworkInterfaceHost(mgmtNode, instanceNode);
		try {
			setHost(sHost);
		} catch (IllegalHostException Ex) {
			String attr = ManagementInterfaceHelper
					.findMgmtInterfaceAttribute(mgmtNode);
			Node netNode = ManagementInterfaceHelper
					.getManagementNetworkInterfaceNode(mgmtNode, instanceNode);
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	private void loadPort(Node mgmtNode) throws ResourcesDescriptorException {
		String sPort = null;
		try {
			sPort = mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_PORT_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_MISSING_ATTR, Common.MGMT_PORT_ATTR));
		}
		try {
			setPort(sPort);
		} catch (IllegalPortException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.MgmtEx_INVALID_ATTR, Common.MGMT_PORT_ATTR), Ex);
		}
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

	private ManagementMethod setManagementMethod(String sMm)
			throws IllegalManagementMethodException {
		if (sMm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return setManagementMethod(ManagementMethod.parseString(sMm));
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

	private Host setHost(String sHost) throws IllegalHostException {
		if (sHost == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return setHost(Host.parseString(sHost));
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

	private Port setPort(String sPort) throws IllegalPortException {
		if (sPort == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return setPort(Port.parseString(sPort));
	}

}
