package com.wat.melody.cloud.disk;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;

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
	public static final String DISK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "disk-devices-selector";

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
	 *         <li><tt>null</tt>, if no Disk Device Management {@link Node} were
	 *         found ;</li>
	 *         </ul>
	 */
	public static Node findDiskManagementNode(Element instanceNode) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceNode,
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

	public static String findDiskDevicesSelector(Element instanceNode) {
		Node mgmtNode = findDiskManagementNode(instanceNode);
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
	 *             if the given Instance {@link Node} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the disk Devices Selector (found in the Disk Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList findDiskDevices(Element instanceNode)
			throws NodeRelatedException {
		String sAllDiskDevSelector = findDiskDevicesSelector(instanceNode);
		try {
			return FilteredDocHelper.getHeritedContent(instanceNode,
					sAllDiskDevSelector);
		} catch (XPathExpressionException Ex) {
			Node mgmtNode = findDiskManagementNode(instanceNode);
			Node attr = mgmtNode.getAttributes().getNamedItem(
					DISK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new NodeRelatedException(attr, Messages.bind(
					Messages.DiskMgmtEx_INVALID_DISK_DEVICES_SELECTOR,
					sAllDiskDevSelector), Ex);
		}
	}

}
