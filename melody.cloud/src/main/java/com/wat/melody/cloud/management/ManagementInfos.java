package com.wat.melody.cloud.management;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.management.exception.IllegalManagementInfosException;
import com.wat.melody.cloud.management.exception.IllegalManagementMethodException;
import com.wat.melody.cloud.management.exception.ManagementException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public class ManagementInfos {

	public static final String DEFAULT_MGMT_DEVICE_SELECTOR = "//network//interface[@device='eth0']";
	public static final String DEFAULT_MGMT_HOST_SELECTOR = "ip";

	public static Node findMgmtNode(Node instanceNode)
			throws ManagementException, ResourcesDescriptorException {
		// Get the management node related to the given node
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode, "//"
					+ Common.MGMT_NODE);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '//" + Common.MGMT_NODE + "'. "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() > 1) {
			// TODO : externalize error message
			throw new ManagementException(
					"This Instance Node contains too many '"
							+ Common.MGMT_NODE
							+ "' XML Nested Element. It should only contains one.");
		} else if (nl.getLength() == 0) {
			// TODO : externalize error message
			throw new ManagementException("This Instance Node contains no '"
					+ Common.MGMT_NODE
					+ "' XML Nested Element. It should only contains one.");
		}
		return nl.item(0);
	}

	public static String findMgmtDeviceSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_DEVICE_SELECTOR_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_DEVICE_SELECTOR;
		}
	}

	public static String findMgmtHostSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_HOST_SELECTOR_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_MGMT_HOST_SELECTOR;
		}
	}

	/*
	 * TODO : put this into XPathExtension, so everybody can use it in the SD
	 */
	public static Node getManagementNetworkDeviceNode(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = ManagementInfos.findMgmtNode(instanceNode);
		} catch (ManagementException Ex) {
			// is raised when management datas are invalid.
			// in this situation, we consider eth0 is the management device
		}
		String sMgmtDeviceSelector = ManagementInfos
				.findMgmtDeviceSelector(mgmtNode);
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					sMgmtDeviceSelector);
			if (nl != null && nl.getLength() > 1) {
				// TODO : error message need more details
				throw new RuntimeException(
						"Multiple Network Management device.");
			} else if (nl == null || nl.getLength() == 0) {
				// search the only device
				nl = GetHeritedContent.getHeritedContent(instanceNode,
						"//network//interface");
			}
		} catch (XPathExpressionException Ex) {
			// TODO : error message need more details
			throw new RuntimeException("not a valid XPath expression.", Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			// TODO : error message need more details
			throw new RuntimeException("No Network Management device.");
		}
		return nl.item(0);
	}

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
	 * <i> * The given node should contains a {@link Common#MGMT_METHOD_ATTR}, a
	 * {@link Common#MGMT_HOST_ATTR} and a {@link Common#MGMT_PORT_ATTR} XML
	 * Attributes ; <BR/>
	 * * The {@link Common#MGMT_METHOD_ATTR} XML attribute must contains a
	 * {@link ManagementMethod} ; <BR/>
	 * * The {@link Common#MGMT_HOST_ATTR} XML attribute must contains a
	 * {@link Host} ; <BR/>
	 * * The {@link Common#MGMT_PORT_ATTR} XML attribute must contains a
	 * {@link Port} ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param context
	 * @param instanceNode
	 * 
	 * @throws ResourcesDescriptorException
	 * @throws ManagementException
	 */
	public ManagementInfos(ITaskContext context, Node instanceNode)
			throws ManagementException, ResourcesDescriptorException {
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		Node mgmtNode = findMgmtNode(instanceNode);
		try {
			loadMethod(mgmtNode);
			loadHost(mgmtNode, instanceNode);
			loadPort(mgmtNode);
		} catch (IllegalManagementInfosException Ex) {
			throw new ManagementException("["
					+ context.getProcessorManager().getResourcesDescriptor()
							.getLocation(mgmtNode).toFullString() + "] "
					+ Ex.getMessage(), Ex.getCause());
		}
	}

	private void loadMethod(Node mgmtNode)
			throws IllegalManagementInfosException {
		String sMethod = null;
		try {
			sMethod = mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_METHOD_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attribute '"
					+ Common.MGMT_METHOD_ATTR + "' is missing.");
		}
		try {
			setManagementMethod(sMethod);
		} catch (IllegalManagementMethodException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attribute '"
					+ Common.MGMT_METHOD_ATTR + "' is invalid.", Ex);
		}
	}

	private void loadHost(Node mgmtNode, Node instanceNode)
			throws IllegalManagementInfosException,
			ResourcesDescriptorException {
		String sHostSelector = findMgmtDeviceSelector(mgmtNode) + "/@"
				+ findMgmtHostSelector(mgmtNode);
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					sHostSelector);
		} catch (XPathExpressionException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attributes '"
					+ Common.MGMT_DEVICE_SELECTOR_ATTR + "' and '"
					+ Common.MGMT_HOST_SELECTOR_ATTR + "' are invalid. "
					+ "Is neither a plain @IP nor a XPath expression.", Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attributes '"
					+ Common.MGMT_DEVICE_SELECTOR_ATTR + "' and '"
					+ Common.MGMT_HOST_SELECTOR_ATTR + "' are invalid. "
					+ "XPath expression doesn't match any nodes.");
		} else if (nl.getLength() > 1) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attributes '"
					+ Common.MGMT_DEVICE_SELECTOR_ATTR + "' and '"
					+ Common.MGMT_HOST_SELECTOR_ATTR + "' are invalid. "
					+ "XPath expression match multiple nodes.");
		}
		try {
			setHost(nl.item(0).getNodeValue());
		} catch (IllegalHostException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attributes '"
					+ Common.MGMT_DEVICE_SELECTOR_ATTR + "' and '"
					+ Common.MGMT_HOST_SELECTOR_ATTR + "' are invalid.", Ex);
		}
	}

	private void loadPort(Node mgmtNode) throws IllegalManagementInfosException {
		String sPort = null;
		try {
			sPort = mgmtNode.getAttributes()
					.getNamedItem(Common.MGMT_PORT_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attribute '"
					+ Common.MGMT_PORT_ATTR + "' is missing.");
		}
		try {
			setPort(sPort);
		} catch (IllegalPortException Ex) {
			// TODO : externalize error message
			throw new IllegalManagementInfosException("Attribute '"
					+ Common.MGMT_PORT_ATTR + "' is invalid.", Ex);
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
