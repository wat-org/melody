package com.wat.melody.cloud.disk;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;

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
	public static final String DELETEONTERMINATION_ATTR = "deleteOnTermination";

	/**
	 * XML attribute of a Disk Device Node, which indicate if the device is the
	 * root device.
	 */
	public static final String ROOTDEVICE_ATTR = "rootDevice";

	private ITaskContext moTC;

	public DiskDevicesLoader(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moTC = tc;
	}

	protected ITaskContext getTC() {
		return moTC;
	}

	private DiskDeviceName loadDeviceName(Node n)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(DEVICE_ATTR);
		if (attr == null) {
			throw new ResourcesDescriptorException(n, Messages.bind(
					Messages.DiskLoadEx_MISSING_ATTR, DEVICE_ATTR));
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceName.parseString(v);
		} catch (IllegalDiskDeviceNameException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private DiskDeviceSize loadDeviceSize(Node n)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(SIZE_ATTR);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceSize.parseString(v);
		} catch (IllegalDiskDeviceSizeException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Boolean loadDeleteOnTermination(Node n) {
		Node attr = n.getAttributes().getNamedItem(DELETEONTERMINATION_ATTR);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	private Boolean loadRootDevice(Node n) {
		Node attr = n.getAttributes().getNamedItem(ROOTDEVICE_ATTR);
		if (attr == null) {
			return null;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	/**
	 * <p>
	 * Converts the given Disk Device {@link Node}s into a
	 * {@link DiskDeviceList}.
	 * </p>
	 * 
	 * <p>
	 * A Disk Device {@link Node} must have the attributes :
	 * <ul>
	 * <li>device : which should contains a {@link DiskDeviceName} (ex:
	 * /dev/sda1, /dev/vda) ;</li>
	 * <li>size : which should contains a {@link DiskDeviceSize} ;</li>
	 * <li>deleteOnTermination : which should contains true/false ;</li>
	 * <li>rootDevice : which should contains true/false ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param nl
	 *            a list of Disk Device {@link Node}s.
	 * 
	 * @return a {@link DiskDeviceList} object, which is a collection of
	 *         {@link DiskDevice} .
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Disk Device
	 *             Node is not valid, multiple root device are found, multiple
	 *             device declare with the same name).
	 */
	public DiskDeviceList load(NodeList nl) throws ResourcesDescriptorException {
		DiskDeviceList dl = new DiskDeviceList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			DiskDeviceName devname = null;
			DiskDeviceSize devsize = null;
			Boolean delonterm = null;
			Boolean isroot = null;
			devname = loadDeviceName(n);
			devsize = loadDeviceSize(n);
			delonterm = loadDeleteOnTermination(n);
			isroot = loadRootDevice(n);

			try {
				dl.addDiskDevice(new DiskDevice(devname, devsize, delonterm,
						isroot));
			} catch (IllegalDiskDeviceListException Ex) {
				throw new ResourcesDescriptorException(n, "This Disk device "
						+ "Node description is not valid. Read message "
						+ "bellow to get more details about this issue.", Ex);
			}
		}
		return dl;
	}

}