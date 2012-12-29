package com.wat.melody.cloud.disk;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.exception.DiskDeviceException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpathextensions.GetHeritedContent;

public abstract class DiskManagementHelper {

	private static Log log = LogFactory.getLog(DiskManagementHelper.class);

	/**
	 * The XML attribute to use in the sequence descriptor
	 */
	public static final String DISK_DEVICE_NODES_SELECTOR_ATTR = "diskDeviceNodesSelector";

	/**
	 * The XML Element which contains Disk Device Management datas in the RD
	 * (e.g. the Disk Device Management Node)
	 */
	public static final String DISK_DEVICES_MGMT_NODE = "disk-management";

	/**
	 * XPath Expression to select Disk Device Management Node in the RD, related
	 * to an Instance Node
	 */
	public static final String DISK_DEVICES_MGMT_NODE_SELECTOR = "//"
			+ DISK_DEVICES_MGMT_NODE;

	/**
	 * The XML attribute of the Disk Device Management Node, which contains the
	 * Disk Device Nodes Selector
	 */
	public static final String DISK_DEVICES_NODE_SELECTOR_ATTRIBUTE = "diskDeviceNodesSelector";

	/**
	 * Default XPath Expression to select Disk Device Nodes in the RD, related
	 * to an Instance Node
	 */
	public static final String DEFAULT_DISK_DEVICES_NODE_SELECTOR = "//"
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
	 * @return the Disk Device Management {@link Node} if found, or
	 *         <code>null</code> otherwise.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 */
	public static Node findDiskDeviceManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(instanceNode,
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
			log.debug(Messages.bind(Messages.DiskMgmtMsg_TOO_MANY_MGMT_NODE,
					DISK_DEVICES_MGMT_NODE, Doc.getNodeLocation(instanceNode)
							.toFullString()));
			return nl.item(nl.getLength() - 1);
		} else if (nl.getLength() == 0) {
			return null;
		}
		return nl.item(0);
	}

	public static String findDiskDeviceManagementSelector(Node instanceNode)
			throws ResourcesDescriptorException {
		Node n = findDiskDeviceManagementNode(instanceNode);
		try {
			return n.getAttributes()
					.getNamedItem(DISK_DEVICES_NODE_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_DISK_DEVICES_NODE_SELECTOR;
		}
	}

	public static void ensureDiskDevicesUpdateIsPossible(
			DiskDeviceList current, DiskDeviceList target)
			throws DiskDeviceException {
		if (current == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (current.size() == 0) {
			throw new RuntimeException("Current Disk Device List is empty. It "
					+ "should at least contains one device, which is the root "
					+ "device. "
					+ "There must be a bug in the current disk list creation.");
		}
		if (target.size() == 0) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_EMPTY_DEVICE_LIST,
					DiskDevicesLoader.DEVICE_ATTR, current.getRootDevice()
							.getDeviceName()));
		}
		if (target.getRootDevice() == null) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_UNDEF_ROOT_DEVICE,
					DiskDevicesLoader.ROOTDEVICE_ATTR, current.getRootDevice()
							.getDeviceName()));
		}
		if (!current.getRootDevice().equals(target.getRootDevice())) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_INCORRECT_ROOT_DEVICE, new Object[] {
							DiskDevicesLoader.ROOTDEVICE_ATTR,
							target.getRootDevice().getDeviceName(),
							current.getRootDevice().getDeviceName() }));
		}
	}

	public static DiskDeviceList computeDiskDevicesToAdd(
			DiskDeviceList current, DiskDeviceList target) {
		DiskDeviceList disksToAdd = new DiskDeviceList(target);
		disksToAdd.removeAll(current);
		return disksToAdd;
	}

	public static DiskDeviceList computeDiskDevicesToRemove(
			DiskDeviceList current, DiskDeviceList target) {
		DiskDeviceList disksToRemove = new DiskDeviceList(current);
		disksToRemove.removeAll(target);
		return disksToRemove;
	}

}
