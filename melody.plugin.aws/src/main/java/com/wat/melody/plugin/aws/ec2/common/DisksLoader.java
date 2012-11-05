package com.wat.melody.plugin.aws.ec2.common;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.plugin.aws.ec2.common.exception.DisksLoaderException;
import com.wat.melody.plugin.aws.ec2.common.exception.IllegalDiskException;
import com.wat.melody.plugin.aws.ec2.common.exception.IllegalDiskListException;

public class DisksLoader {

	public static final String SIZE_ATTR = "size";

	public static final String DEVICE_ATTR = "device";

	public static final String DELETEONTERMINATION_ATTR = "deleteOnTermination";

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

	protected IResourcesDescriptor getED() {
		return getTC().getProcessorManager().getResourcesDescriptor();
	}

	private void loadDevice(Node n, Disk disk) throws DisksLoaderException {
		Node attr = n.getAttributes().getNamedItem(DEVICE_ATTR);
		if (attr == null) {
			return;
		}
		String v = attr.getNodeValue();
		if (v.length() == 0) {
			return;
		}
		try {
			disk.setDevice(v);
		} catch (IllegalDiskException Ex) {
			throw new DisksLoaderException(Messages.bind(
					Messages.DiskLoadEx_INVALID_ATTR, v, DEVICE_ATTR), Ex);
		}
	}

	private void loadGiga(Node n, Disk disk) throws DisksLoaderException {
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
			throw new DisksLoaderException(Messages.bind(
					Messages.DiskLoadEx_INVALID_ATTR, v, SIZE_ATTR), Ex);
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

	private void loadDisk(Node n, Disk disk) throws DisksLoaderException {
		loadDevice(n, disk);
		loadGiga(n, disk);
		loadDeleteOnTermination(n, disk);
		loadRootDevice(n, disk);

		if (disk.getDevice() == null) {
			throw new DisksLoaderException(Messages.bind(
					Messages.DiskLoadEx_MISSING_ATTR, DEVICE_ATTR));
		}
	}

	/**
	 * <p>
	 * Converts selected Disk <code>Node</code>s into a {@link DiskList}. The
	 * given XPath Expression selects Disk <code>Node</code>s.
	 * </p>
	 * 
	 * <p>
	 * <i>A Disk <code>Node</code> must have the attributes : <BR/>
	 * * size : which should contains a SIZE ; <BR/>
	 * * device : which should contains a LINUX DEVICE NAME ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sDisksXpr
	 *            a String containing the XPath Expression which will be used to
	 *            found <code>Node</code>s.
	 * 
	 * @return a {@link DiskList} object, which is a collection of {@link Disk}
	 *         .
	 * 
	 * @throws DisksLoaderException
	 *             if the conversion failed (ex : the XPath Expression is not
	 *             well-formed, or the content of an attribute is not valid)
	 */
	public DiskList load(NodeList nl) throws DisksLoaderException {
		DiskList dl = new DiskList();
		for (int i = 0; i < nl.getLength(); i++) {
			try {
				Disk disk = new Disk();
				loadDisk(nl.item(i), disk);
				dl.addDisk(disk);
			} catch (DisksLoaderException | IllegalDiskListException Ex) {
				throw new DisksLoaderException(Messages.DiskLoadEx_MANAGED, Ex);
			}
		}
		return dl;
	}
}
