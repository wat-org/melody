package com.wat.melody.cloud.disk;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.exception.DiskException;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class DiskManagementHelper {

	/**
	 * The XML Element which contains Disk Management datas in the RD
	 */
	public static final String DISK_MGMT_NODE = "disk-management";

	/**
	 * XPath Expression to select Disk Management Node in the RD, related to an
	 * Instance Node
	 */
	public static final String DISK_MGMT_NODE_SELECTOR = "//" + DISK_MGMT_NODE;

	/**
	 * The XML attribute of the Disk Management Node, which contains the Disk
	 * Nodes Selector
	 */
	public static final String DISKS_NODE_SELECTOR_ATTRIBUTE = "diskNodeSelector";

	/**
	 * Default XPath Expression to select Disk Nodes in the RD, related to an
	 * Instance Node
	 */
	public static final String DEFAULT_DISKS_NODE_SELECTOR = "//"
			+ DisksLoader.DISK_NE;

	/**
	 * 
	 * @param instanceNode
	 * 
	 * @return the Disk Management {@link Node} if found, or <code>null</code>
	 *         otherwise.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if multiple Disk Management {@link Node} were found.
	 * @throws ResourcesDescriptorException
	 *             if an error in HERIT is found
	 */
	public static Node findDiskManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
					DISK_MGMT_NODE_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '" + DISK_MGMT_NODE_SELECTOR
					+ "'. " + "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() > 1) {
			throw new ResourcesDescriptorException(instanceNode,
					Messages.bind(Messages.DiskMgmtEx_TOO_MANY_DISK_MGMT_NODE,
							DISK_MGMT_NODE));
		} else if (nl.getLength() == 0) {
			return null;
		}
		return nl.item(0);
	}

	public static String findDiskManagementSelector(Node instanceNode)
			throws ResourcesDescriptorException {
		Node n = findDiskManagementNode(instanceNode);
		try {
			return n.getAttributes()
					.getNamedItem(DISKS_NODE_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_DISKS_NODE_SELECTOR;
		}
	}

	public static void ensureDiskUpdateIsPossible(DiskList current,
			DiskList target) throws DiskException {
		if (target.getRootDevice() == null) {
			throw new DiskException(Messages.bind(
					Messages.DiskDefEx_UNDEF_ROOT_DEVICE,
					DisksLoader.ROOTDEVICE_ATTR, current.getRootDevice()
							.getDevice()));
		}
		if (!current.getRootDevice().equals(target.getRootDevice())) {
			throw new DiskException(Messages.bind(
					Messages.DiskDefEx_INCORRECT_ROOT_DEVICE, new Object[] {
							DisksLoader.ROOTDEVICE_ATTR,
							target.getRootDevice().getDevice(),
							current.getRootDevice().getDevice() }));
		}
	}

	public static DiskList computeDiskToAdd(DiskList current, DiskList target) {
		DiskList disksToAdd = new DiskList(target);
		disksToAdd.removeAll(current);
		return disksToAdd;
	}

	public static DiskList computeDiskToRemove(DiskList current, DiskList target) {
		DiskList disksToRemove = new DiskList(current);
		disksToRemove.removeAll(target);
		return disksToRemove;
	}

}
