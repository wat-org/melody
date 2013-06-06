package com.wat.melody.cloud.disk;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDevicesLoader {

	/**
	 * XML Nested element of an Instance Element Node, which contains the
	 * definition of a Disk Device.
	 */
	public static final String DISK_DEVICE_NE = "disk";

	/**
	 * XML attribute of a Disk Device Element Node, which define the name of the
	 * device.
	 */
	public static final String DEVICE_NAME_ATTR = "device-name";

	/**
	 * XML attribute of a Disk Device Element Node, which define the size of the
	 * device.
	 */
	public static final String SIZE_ATTR = "size";

	/**
	 * XML attribute of a Disk Device Element Node, which indicate if the device
	 * should be automatically deleted when the instance is deleted.
	 */
	public static final String DELETEONTERMINATION_ATTR = "delete-on-termination";

	/**
	 * XML attribute of a Disk Device Element Node, which indicate if the device
	 * is the root device.
	 */
	public static final String ROOTDEVICE_ATTR = "root-device";

	/**
	 * XML attribute of a Disk Device Element Node, which indicate if the
	 * timeout of the device creation operation.
	 */
	public static final String TIMEOUT_CREATE_ATTR = "timeout-create";

	/**
	 * XML attribute of a Disk Device Element Node, which indicate if the
	 * timeout of the device attachment operation.
	 */
	public static final String TIMEOUT_ATTACH_ATTR = "timeout-attach";

	/**
	 * XML attribute of a Disk Device Element Node, which indicate if the
	 * timeout of the device detachment operation.
	 */
	public static final String TIMEOUT_DETACH_ATTR = "timeout-detach";

	public DiskDevicesLoader() {
	}

	private DiskDeviceName loadDeviceName(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, DEVICE_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceName.parseString(v);
		} catch (IllegalDiskDeviceNameException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					DEVICE_NAME_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private DiskDeviceSize loadDeviceSize(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, SIZE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return DiskDeviceSize.parseString(v);
		} catch (IllegalDiskDeviceSizeException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e, SIZE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private Boolean loadDeleteOnTermination(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e,
				DELETEONTERMINATION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	private Boolean loadRootDevice(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, ROOTDEVICE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return Boolean.parseBoolean(v);
	}

	private GenericTimeout loadCreateTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_CREATE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_CREATE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadAttachTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_ATTACH_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_ATTACH_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadDetachTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_DETACH_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_DETACH_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Find the Disk Device {@link Element}s of the given Instance
	 * {@link Element} and convert it into a {@link DiskDeviceList}.
	 * </p>
	 * 
	 * <p>
	 * A Disk Device {@link Element} may have the attributes :
	 * <ul>
	 * <li>device : (mandatory) which should contains a {@link DiskDeviceName}
	 * (ex: /dev/sda1, /dev/vda) ;</li>
	 * <li>size : which should contains a {@link DiskDeviceSize} ;</li>
	 * <li>delete-on-termination : which should contains a <tt>Boolean</tt> ;</li>
	 * <li>root-device : which should contains a <tt>Boolean</tt> ;</li>
	 * <li>timeout-create : which should contains a {@link GenericTimeout} ;</li>
	 * <li>timeout-attach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>timeout-detach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an Instance {@link Element}.
	 * 
	 * @return a {@link DiskDeviceList} object, which is a collection of
	 *         {@link DiskDevice}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of a Disk Device
	 *             {@link Element} is not valid, multiple root device are found,
	 *             multiple device declare with the same name).
	 */
	public DiskDeviceList load(Element instanceElmt)
			throws NodeRelatedException {
		NodeList nl = DiskManagementHelper.findDiskDevices(instanceElmt);

		DiskDeviceList dl = new DiskDeviceList();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			DiskDeviceName devname = loadDeviceName(e);
			if (devname == null) {
				throw new NodeRelatedException(e,
						Messages.bind(Messages.DiskDevLoaderEx_MISSING_ATTR,
								DEVICE_NAME_ATTR));
			}
			DiskDeviceSize devsize = loadDeviceSize(e);
			Boolean delonterm = loadDeleteOnTermination(e);
			Boolean isroot = loadRootDevice(e);
			GenericTimeout createTimeout = loadCreateTimeout(e);
			GenericTimeout attachTimeout = loadAttachTimeout(e);
			GenericTimeout detachTimeout = loadDetachTimeout(e);

			try {
				dl.addDiskDevice(new DiskDevice(devname, devsize, delonterm,
						isroot, createTimeout, attachTimeout, detachTimeout));
			} catch (IllegalDiskDeviceListException Ex) {
				throw new NodeRelatedException(e,
						Messages.DiskDevLoaderEx_GENERIC_ERROR, Ex);
			}
		}
		return dl;
	}

}