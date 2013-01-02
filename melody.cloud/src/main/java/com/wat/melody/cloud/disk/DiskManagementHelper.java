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
	 * XML attribute in the SD, which contains an XPath Expression which select
	 * Disk Device Nodes.
	 */
	public static final String DISK_DEVICE_NODES_SELECTOR_ATTR = "diskDevicesSelector";

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
	public static final String DISK_DEVICES_NODE_SELECTOR_ATTRIBUTE = "diskDevicesSelector";

	/**
	 * Default XPath Expression to select Disk Device Nodes in the RD, related
	 * to an Instance Node
	 */
	public static final String DEFAULT_DISK_DEVICES_NODE_SELECTOR = "//"
			+ DiskDevicesLoader.DISK_DEVICE_NE;

	/**
	 * <p>
	 * Return the Disk Management {@link Node} related to the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return <ul>
	 *         <li>The Disk Management {@link Node} related to the given
	 *         Instance {@link Node}, if one Disk Management {@link Node} is
	 *         found ;</li>
	 *         <li>The last Disk Management {@link Node} related to the given
	 *         Instance {@link Node}, if multiple Disk Management {@link Node}
	 *         were found ;</li>
	 *         <li><code>null</code>, if no Disk Management {@link Node} were
	 *         found ;</li>
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

	public static String findDiskDevicesSelector(Node instanceNode)
			throws ResourcesDescriptorException {
		Node n = findDiskManagementNode(instanceNode);
		try {
			return n.getAttributes()
					.getNamedItem(DISK_DEVICES_NODE_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_DISK_DEVICES_NODE_SELECTOR;
		}
	}

}
