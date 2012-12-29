package com.wat.melody.cloud.disk;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.exception.IllegalDiskException;
import com.wat.melody.cloud.disk.exception.IllegalDiskListException;

public class DisksLoader {

	/**
	 * The 'disk' XML Nested element of the Instance Node in the RD
	 */
	public static final String DISK_NE = "disk";

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

	public DisksLoader(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moTC = tc;
	}

	protected ITaskContext getTC() {
		return moTC;
	}

	private void loadDevice(Node n, Disk disk)
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
			disk.setDevice(v);
		} catch (IllegalDiskException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private void loadGiga(Node n, Disk disk)
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
		} catch (IllegalDiskException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private void loadDeleteOnTermination(Node n, Disk disk) {
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

	private void loadRootDevice(Node n, Disk disk) {
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
	 * Converts the given Disk <code>Node</code>s into a {@link DiskList}.
	 * </p>
	 * 
	 * <p>
	 * <i>A Disk <code>Node</code> must have the attributes : <BR/>
	 * * size : which should contains a SIZE ; <BR/>
	 * * device : which should contains a LINUX DEVICE NAME ; <BR/>
	 * * deleteOnTermination : which should contains true/false ; <BR/>
	 * * rootDevice : which should contains true/false ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param nl
	 *            a list of Disk <code>Node</code>s.
	 * 
	 * @return a {@link DiskList} object, which is a collection of {@link Disk}
	 *         .
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a Disk Node'n
	 *             attribute is not valid)
	 */
	public DiskList load(NodeList nl) throws ResourcesDescriptorException {
		DiskList dl = new DiskList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			Disk disk = new Disk();
			loadDevice(n, disk);
			loadGiga(n, disk);
			loadDeleteOnTermination(n, disk);
			loadRootDevice(n, disk);

			try {
				dl.addDisk(disk);
			} catch (IllegalDiskListException Ex) {
				throw new ResourcesDescriptorException(n, "This Disk Node "
						+ "description is not no valid. Read message bellow "
						+ "to get more details about this issue.", Ex);
			}
		}
		return dl;
	}

}