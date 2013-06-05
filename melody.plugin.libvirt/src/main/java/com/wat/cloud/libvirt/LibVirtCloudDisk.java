package com.wat.cloud.libvirt;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDeviceName;
import com.wat.melody.cloud.disk.DiskDeviceSize;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.exception.XPathExpressionSyntaxException;

/**
 * <p>
 * Quick and dirty class which provide libvirt disk management features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudDisk {

	private static Log log = LogFactory.getLog(LibVirtCloudDisk.class);

	public static DiskDeviceList getDiskDevices(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			DiskDeviceList dl = new DiskDeviceList();
			Doc ddoc = LibVirtCloud.getDomainXMLDesc(d);
			NodeList nl = ddoc
					.evaluateAsNodeList("/domain/devices/disk[@device='disk']");
			for (int i = 0; i < nl.getLength(); i++) {
				String volPath = XPathExpander.evaluateAsString("source/@file",
						nl.item(i));
				StorageVol sv = d.getConnect().storageVolLookupByPath(volPath);
				DiskDeviceName devname = DiskDeviceName.parseString("/dev/"
						+ XPathExpander.evaluateAsString("target/@dev",
								nl.item(i)));
				DiskDeviceSize devsize = DiskDeviceSize.parseInt((int) (sv
						.getInfo().capacity / (1024 * 1024 * 1024)));
				Boolean delonterm = true;
				Boolean isroot = devname.getValue().equals("/dev/vda");
				dl.addDiskDevice(new DiskDevice(devname, devsize, delonterm,
						isroot));
			}
			if (dl.size() == 0) {
				throw new RuntimeException("Failed to build Domain '"
						+ d.getName()
						+ "' Disk Device List. No Disk Device found ");
			}
			if (dl.getRootDevice() == null) {
				throw new RuntimeException("Failed to build Domain '"
						+ d.getName()
						+ "' Disk Device List. No Root Disk Device found ");
			}
			return dl;
		} catch (XPathExpressionException | IllegalDiskDeviceSizeException
				| LibvirtException | IllegalDiskDeviceNameException
				| IllegalDiskDeviceListException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String DETACH_DISK_DEVICE_XML_SNIPPET = "<disk type='file' device='disk'>"
			+ "<source file='§[volPath]§'/>"
			+ "<target dev='§[device]§' bus='virtio'/>" + "</disk>";

	public static void detachAndDeleteDiskDevices(Domain d,
			DiskDeviceList disksToRemove) {
		if (disksToRemove == null || disksToRemove.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}

		// preparation de la variabilisation du XML
		PropertySet vars = new PropertySet();
		try {
			Doc ddoc = LibVirtCloud.getDomainXMLDesc(d);
			// pour chaque disque a supprimer
			for (DiskDevice disk : disksToRemove) {
				String deviceToRemove = DiskDeviceNameConverter.convert(disk
						.getDiskDeviceName());
				// search the path of the disk which match device to remove
				String volPath = ddoc
						.evaluateAsString("/domain/devices/disk[@device='disk' and target/@dev='"
								+ deviceToRemove + "']/source/@file");
				if (volPath == null) {
					throw new RuntimeException("Domain '" + d.getName()
							+ "' has no disk device '" + deviceToRemove + "' !");
				}
				// variabilisation du detachement de la device
				vars.put(new Property("device", deviceToRemove));
				vars.put(new Property("volPath", volPath));
				// detachement de la device
				log.trace("Detaching Disk Device '" + disk.getDiskDeviceName()
						+ "' (" + disk.getSize() + " Go) on Domain '"
						+ d.getName() + "' ...");
				int flag = LibVirtCloud.getDomainState(d) == InstanceState.RUNNING ? 3
						: 2;
				d.detachDeviceFlags(XPathExpander.expand(
						DETACH_DISK_DEVICE_XML_SNIPPET, null, vars), flag);
				log.trace("Disk Device '" + disk.getDiskDeviceName() + "' ("
						+ disk.getSize() + " Go) detached on Domain '"
						+ d.getName() + "'.");
				// suppression du volume
				deleteDiskDevice(d, disk);
			}
		} catch (LibvirtException | IllegalPropertyException
				| XPathExpressionException | XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void deleteDiskDevice(Domain d, DiskDevice disk) {
		if (disk == null) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			// recuperation du storage pool
			StoragePool sp = cnx.storagePoolLookupByName("default");
			// search the path of the disk which match device to remove
			StorageVol sv = sp.storageVolLookupByName(getVolumeName(d, disk));
			String volPath = sv.getPath();
			log.trace("Deleting Disk Device '" + disk.getDiskDeviceName()
					+ "' on domain '" + sInstanceId
					+ "' ... LibVirt Volume path is '" + volPath + "'.");
			sv.delete(0);
			log.debug("Disk Device '" + disk.getDiskDeviceName()
					+ "' deleted on Domain '" + sInstanceId + "'.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String getVolumeName(Domain d, DiskDevice disk) {
		if (disk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + DiskDevice.class.getCanonicalName()
					+ ".");
		}
		try {
			String diskdevname = DiskDeviceNameConverter.convert(disk
					.getDiskDeviceName());
			return d.getName() + "-" + diskdevname + ".img";
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String NEW_DISK_DEVICE_XML_SNIPPET = "<volume>"
			+ "<name>§[vmName]§-§[device]§.img</name>" + "<source>"
			+ "</source>" + "<capacity unit='bytes'>§[capacity]§</capacity>"
			+ "<allocation unit='bytes'>§[allocation]§</allocation>"
			+ "<target>" + "<format type='raw'/>" + "</target>" + "</volume>";

	public static final String ATTACH_DISK_DEVICE_XML_SNIPPET = "<disk type='file' device='disk'>"
			+ "<driver name='qemu' type='raw' cache='none'/>"
			+ "<source file='§[volPath]§'/>"
			+ "<target dev='§[device]§' bus='virtio'/>" + "</disk>";

	public static void createAndAttachDiskDevices(Domain d,
			DiskDeviceList disksToAdd) {
		if (disksToAdd == null || disksToAdd.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}

		try {
			// recuperation du storage pool
			StoragePool sp = d.getConnect().storagePoolLookupByName("default");
			// preparation de la variabilisation des template XML
			PropertySet vars = new PropertySet();
			vars.put(new Property("vmName", d.getName()));
			vars.put(new Property("allocation", "0"));
			// pour chaque disque
			for (DiskDevice disk : disksToAdd) {
				// variabilisation de la creation du volume
				String deviceToAdd = DiskDeviceNameConverter.convert(disk
						.getDiskDeviceName());
				vars.put(new Property("device", deviceToAdd));
				vars.put(new Property("capacity", String.valueOf((long) disk
						.getSize() * 1024 * 1024 * 1024)));
				// creation du volume
				log.trace("Creating Disk Device '" + disk.getDiskDeviceName()
						+ "' (" + disk.getSize() + " Go) for Domain '"
						+ d.getName() + "' ...");
				StorageVol sv = sp.storageVolCreateXML(XPathExpander.expand(
						NEW_DISK_DEVICE_XML_SNIPPET, null, vars), 0);
				log.debug("Disk Device '" + disk.getDiskDeviceName() + "' ("
						+ disk.getSize() + " Go) created for Domain '"
						+ d.getName() + "'. LibVirt Volume path is '"
						+ sv.getPath() + "'.");

				// variabilisation de l'attachement du volume
				vars.put(new Property("volPath", sv.getPath()));
				// attachement de la device
				log.trace("Attaching Disk Device '" + disk.getDiskDeviceName()
						+ "' on Domain '" + d.getName() + "' ...");
				int flag = LibVirtCloud.getDomainState(d) == InstanceState.RUNNING ? 3
						: 2;
				d.attachDeviceFlags(XPathExpander.expand(
						ATTACH_DISK_DEVICE_XML_SNIPPET, null, vars), flag);
				log.debug("Disk Device '" + disk.getDiskDeviceName()
						+ "' attached on Domain '" + d.getName() + "'.");
			}
		} catch (LibvirtException | IllegalPropertyException
				| XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

}

abstract class DiskDeviceNameConverter {

	/**
	 * <p>
	 * Converts the given {@link DiskDeviceName} to the libvirt name of the disk
	 * device.
	 * </p>
	 * 
	 * @param diskdev
	 *            is the {@link DiskDeviceName} to convert.
	 * 
	 * @return the libvirt name of the disk device.
	 */
	public static String convert(DiskDeviceName diskdev) {
		if (diskdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceName.class.getCanonicalName() + ".");
		}
		return diskdev.getValue().replace("/dev/", "");
	}

}