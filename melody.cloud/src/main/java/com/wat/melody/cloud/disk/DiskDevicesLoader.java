package com.wat.melody.cloud.disk;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDevicesLoader {

	/**
	 * XML Nested element of an Instance Node, which contains the definition of
	 * a Disk Device.
	 */
	public static final String DISK_DEVICE_NE = "disk";

	/**
	 * XML attribute of a Disk Device Node, which define the name of the device.
	 */
	public static final String DEVICE_ATTR = "device";

	/**
	 * XML attribute of a Disk Device Node, which define the size of the device.
	 */
	public static final String SIZE_ATTR = "size";

	/**
	 * XML attribute of a Disk Device Node, which indicate if the device should
	 * be automatically deleted when the instance is deleted.
	 */
	public static final String DELETEONTERMINATION_ATTR = "delete-on-termination";

	/**
	 * XML attribute of a Disk Device Node, which indicate if the device is the
	 * root device.
	 */
	public static final String ROOTDEVICE_ATTR = "root-device";

	public DiskDevicesLoader() {
	}

	private DiskDeviceName loadDeviceName(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DEVICE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceName.parseString(v);
		} catch (IllegalDiskDeviceNameException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, DEVICE_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private DiskDeviceSize loadDeviceSize(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, SIZE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceSize.parseString(v);
		} catch (IllegalDiskDeviceSizeException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, SIZE_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Boolean loadDeleteOnTermination(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n,
				DELETEONTERMINATION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	private Boolean loadRootDevice(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, ROOTDEVICE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	/**
	 * <p>
	 * Find the disk Device {@link Node}s of the given Instance {@link Node} and
	 * convert it into a {@link DiskDeviceList}.
	 * </p>
	 * 
	 * <p>
	 * A Disk Device {@link Node} must have the attributes :
	 * <ul>
	 * <li>device : which should contains a {@link DiskDeviceName} (ex:
	 * /dev/sda1, /dev/vda) ;</li>
	 * <li>size : which should contains a {@link DiskDeviceSize} ;</li>
	 * <li>delete-on-termination : which should contains a Boolean ;</li>
	 * <li>root-device : which should contains a Boolean ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return a {@link DiskDeviceList} object, which is a collection of
	 *         {@link DiskDevice}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Node} is <code>null</code> or is
	 *             not an element {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Disk Device
	 *             {@link Node} is not valid, multiple root device are found,
	 *             multiple device declare with the same name).
	 */
	public DiskDeviceList load(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = DiskManagementHelper.findDiskDevices(instanceNode);

		DiskDeviceList dl = new DiskDeviceList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			DiskDeviceName devname = loadDeviceName(n);
			if (devname == null) {
				throw new ResourcesDescriptorException(n, Messages.bind(
						Messages.DiskDevLoaderEx_MISSING_ATTR, DEVICE_ATTR));
			}
			DiskDeviceSize devsize = loadDeviceSize(n);
			Boolean delonterm = loadDeleteOnTermination(n);
			Boolean isroot = loadRootDevice(n);

			try {
				dl.addDiskDevice(new DiskDevice(devname, devsize, delonterm,
						isroot));
			} catch (IllegalDiskDeviceListException Ex) {
				throw new ResourcesDescriptorException(n,
						Messages.DiskDevLoaderEx_GENERIC_ERROR, Ex);
			}
		}
		return dl;
	}

}