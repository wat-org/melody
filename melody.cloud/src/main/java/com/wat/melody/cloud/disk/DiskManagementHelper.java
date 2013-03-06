package com.wat.melody.cloud.disk;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DiskManagementHelper {

	/**
	 * XML Element in the RD, which contains Disk Device Management datas of the
	 * related Instance Node (more formally called the
	 * "Disk Device Management Node")
	 */
	public static final String DISK_DEVICES_MGMT_NE = "disk-management";

	/**
	 * XPath Expression which select the Disk Device Management Node of the
	 * related Instance Node
	 */
	public static final String DISK_DEVICES_MGMT_NODE_SELECTOR = "//"
			+ DISK_DEVICES_MGMT_NE;

	/**
	 * XML attribute of the Disk Device Management Node, which contains the Disk
	 * Device Nodes Selector
	 */
	public static final String DISK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "diskDevicesSelector";

	/**
	 * Default XPath Expression to select Disk Device Nodes in the RD, related
	 * to an Instance Node
	 */
	public static final String DEFAULT_DISK_DEVICE_NODES_SELECTOR = "//"
			+ DiskDevicesLoader.DISK_DEVICE_NE;

	/**
	 * <p>
	 * Return the Disk Device Management {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return <ul>
	 *         <li>The Disk Device Management {@link Node} related to the given
	 *         Instance {@link Node}, if one Disk Device Management {@link Node}
	 *         is found ;</li>
	 *         <li>The last Disk Device Management {@link Node} related to the
	 *         given Instance {@link Node}, if multiple Disk Device Management
	 *         {@link Node} were found ;</li>
	 *         <li><code>null</code>, if no Disk Device Management {@link Node}
	 *         were found ;</li>
	 *         </ul>
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 */
	public static Node findDiskManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = XPathHelper.getHeritedContent(instanceNode,
					DISK_DEVICES_MGMT_NODE_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '"
					+ DISK_DEVICES_MGMT_NODE_SELECTOR + "'. "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() > 1) {
			return nl.item(nl.getLength() - 1);
		} else if (nl.getLength() == 0) {
			return null;
		}
		return nl.item(0);
	}

	public static String findDiskDevicesSelector(Node instanceNode) {
		Node mgmtNode = null;
		try {
			mgmtNode = findDiskManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(DISK_DEVICE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_DISK_DEVICE_NODES_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Disk Device {@link Node}s of the given Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return The Disk Device {@link Node}s of the given Instance {@link Node}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if the disk Devices Selector (found in the Disk Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList findDiskDevices(Node instanceNode)
			throws ResourcesDescriptorException {
		String sAllDiskDevSelector = findDiskDevicesSelector(instanceNode);
		try {
			return XPathHelper.getHeritedContent(instanceNode,
					sAllDiskDevSelector);
		} catch (XPathExpressionException Ex) {
			Node mgmtNode = findDiskManagementNode(instanceNode);
			Node attr = mgmtNode.getAttributes().getNamedItem(
					DISK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
					Messages.DiskMgmtEx_INVALID_DISK_DEVICES_SELECTOR,
					sAllDiskDevSelector), Ex);
		}
	}

}
