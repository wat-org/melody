package com.wat.melody.cloud.disk;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;

public class DiskDevicesLoader {

	/**
	 * The 'disk' XML Nested element of the Instance Node in the RD
	 */
	public static final String DISK_DEVICE_NE = "disk";

	/**
	 * The 'size' XML attribute of a Disk Node
	 */
	public static final String SIZE_ATTR = "size";

	/**
	 * The 'device' XML attribute of a Disk Node
	 */
	public static final String DEVICE_ATTR = "device";

	/**
	 * The 'deleteOnTermination' XML attribute of a Disk Node
	 */
	public static final String DELETEONTERMINATION_ATTR = "deleteOnTermination";

	/**
	 * The 'rootDevice' XML attribute of a Disk Node
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

	private void loadDevice(Node n, DiskDevice disk)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(DEVICE_ATTR);
		if (attr == null) {
			throw new ResourcesDescriptorException(n, Messages.bind(
					Messages.DiskLoadEx_MISSING_ATTR, DEVICE_ATTR));
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return;
		}
		try {
			disk.setDeviceName(v);
		} catch (IllegalDiskDeviceException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private void loadGiga(Node n, DiskDevice disk)
			throws ResourcesDescriptorException {
		Node attr = n.getAttributes().getNamedItem(SIZE_ATTR);
		if (attr == null) {
			return;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return;
		}
		try {
			disk.setSize(v);
		} catch (IllegalDiskDeviceException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private void loadDeleteOnTermination(Node n, DiskDevice disk) {
		Node attr = n.getAttributes().getNamedItem(DELETEONTERMINATION_ATTR);
		if (attr == null) {
			return;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return;
		}
		disk.setDeleteOnTermination(Boolean.parseBoolean(v));
	}

	private void loadRootDevice(Node n, DiskDevice disk) {
		Node attr = n.getAttributes().getNamedItem(ROOTDEVICE_ATTR);
		if (attr == null) {
			return;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return;
		}
		disk.setRootDevice(Boolean.parseBoolean(v));
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
	 * <li>size : which should contains a SIZE in Go ;</li>
	 * <li>device : which should contains a LINUX DEVICE NAME (ex: /dev/sda1,
	 * /dev/vda) ;</li>
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
			DiskDevice disk = new DiskDevice();
			loadDevice(n, disk);
			loadGiga(n, disk);
			loadDeleteOnTermination(n, disk);
			loadRootDevice(n, disk);

			try {
				dl.addDiskDevice(disk);
			} catch (IllegalDiskDeviceListException Ex) {
				throw new ResourcesDescriptorException(n, "This Disk device "
						+ "Node description is not valid. Read message "
						+ "bellow to get more details about this issue.", Ex);
			}
		}
		return dl;
	}

}