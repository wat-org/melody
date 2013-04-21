package com.wat.cloud.libvirt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.Error.ErrorNumber;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDeviceName;
import com.wat.melody.cloud.disk.DiskDeviceSize;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.Direction;
import com.wat.melody.common.network.FwRuleDecomposed;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.common.network.Interface;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.Protocol;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.exception.XPathExpressionSyntaxException;
import com.wat.melody.plugin.libvirt.common.InstanceStateConverter;

/**
 * <p>
 * Quick and dirty class which provide access to libvirt features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloud {

	private static Log log = LogFactory.getLog(LibVirtCloud.class);

	public static final String LIBVIRT_CLOUD_IMG_CONF = "/Cloud/libvirt/conf.xml";
	private static Doc conf = loadLibVirtCloudConfiguration();

	public static final String LIBVIRT_CLOUD_NET_CONF = "/Cloud/libvirt/net-default.xml";
	private static Doc netconf = loadLibVirtCloudNetworkConfiguration();

	public static final String LIBVIRT_CLOUD_SIZE_CONF = "/Cloud/libvirt/instance-sizing.xml";
	private static Doc sizeconf = loadLibVirtCloudSizingConfiguration();

	private static Doc loadLibVirtCloudConfiguration() {
		Doc doc = new Doc();
		try {
			doc.load(LIBVIRT_CLOUD_IMG_CONF);
			validateImages(doc);
		} catch (MelodyException | IOException Ex) {
			throw new RuntimeException(
					"Failed to load LibVirtCloud Configuration File '"
							+ LIBVIRT_CLOUD_IMG_CONF + "'.", Ex);
		}
		return doc;
	}

	private static Doc loadLibVirtCloudNetworkConfiguration() {
		Doc doc = new Doc();
		try {
			doc.load(LIBVIRT_CLOUD_NET_CONF);
		} catch (MelodyException | IOException Ex) {
			throw new RuntimeException(
					"Failed to load LibVirtCloud Network Configuration File '"
							+ LIBVIRT_CLOUD_IMG_CONF + "'.", Ex);
		}
		return doc;
	}

	private static Doc loadLibVirtCloudSizingConfiguration() {
		Doc doc = new Doc();
		try {
			doc.load(LIBVIRT_CLOUD_SIZE_CONF);
		} catch (MelodyException | IOException Ex) {
			throw new RuntimeException(
					"Failed to load LibVirtCloud Network Configuration File '"
							+ LIBVIRT_CLOUD_IMG_CONF + "'.", Ex);
		}
		return doc;
	}

	private static void validateImages(Doc doc) {
		NodeList nl = null;
		try {
			nl = doc.evaluateAsNodeList("/libvirtcloud/images/image");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.", Ex);
		}
		if (nl == null) {
			return;
		}
		for (int j = 0; j < nl.getLength(); j++) {
			Node n = nl.item(j);
			Node name = n.getAttributes().getNamedItem("name");
			if (name == null) {
				throw new RuntimeException("Image is not valid. "
						+ "'name' XML attribute cannot be found.");
			}
			String sImageId = name.getNodeValue();
			Node descriptor = n.getAttributes().getNamedItem("descriptor");
			if (descriptor == null) {
				throw new RuntimeException("Image '" + sImageId
						+ "' is not valid. "
						+ "'descriptor' XML attribute cannot be found.");
			}

			if (n.getChildNodes().getLength() == 0) {
				throw new RuntimeException("Image '" + sImageId
						+ "' is not valid. "
						+ "No 'disk' XML Nested Element can be found.");
			}
			for (int i = 0; i < n.getChildNodes().getLength(); i++) {
				Node disk = n.getChildNodes().item(i);
				if (disk.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				if (!disk.getNodeName().equals("disk")) {
					throw new RuntimeException("Image '" + sImageId
							+ "' is not valid. " + "XML Nested element n°" + i
							+ " is not a 'disk'.");
				}
				descriptor = disk.getAttributes().getNamedItem("descriptor");
				if (descriptor == null) {
					throw new RuntimeException("Image '" + sImageId
							+ "' is not valid. "
							+ "'descriptor' XML attribute cannot be found for "
							+ "Disk Nested element n°" + i + ".");
				}
				try {
					FS.validateFileExists(descriptor.getNodeValue());
				} catch (IllegalFileException Ex) {
					throw new RuntimeException("Image '" + sImageId
							+ "' is not valid. "
							+ "'descriptor' XML attribute for Disk Nested "
							+ "element n°" + i + " doens't contains a valid "
							+ "file path.", Ex);
				}
				Node source = disk.getAttributes().getNamedItem("source");
				if (source == null) {
					throw new RuntimeException("Image '" + sImageId
							+ "' is not valid. "
							+ "'source' XML attribute cannot be found for "
							+ "Disk Nested element n°" + i + ".");
				}
				Node device = disk.getAttributes().getNamedItem("device");
				if (device == null) {
					throw new RuntimeException("Image '" + sImageId
							+ "' is not valid. "
							+ "'device' XML attribute cannot be found for "
							+ "Disk Nested element n°" + i + ".");
				}
			}
		}
	}

	public static final String SIZE_PATTERN = "[0-9]+([.][0-9]+)?";

	/**
	 * 
	 * @param type
	 * 
	 * @return the amount of RAM (in Kilo Octet) corresponding to the given
	 *         {@link InstanceType}.
	 */
	public static int getRAM(InstanceType type) {
		String sRam = null;
		try {
			sRam = sizeconf.evaluateAsString("/sizings/sizing[@name='" + type
					+ "']/@ram");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (!sRam.matches("^" + SIZE_PATTERN + "$")) {
			throw new RuntimeException(sizeconf.getFileFullPath()
					+ ": instance type '" + type
					+ "' have an invalid ram attribute. '" + sRam
					+ "' doesn't match pattern '" + SIZE_PATTERN + "'.");
		}
		return (int) (Float.parseFloat(sRam) * 1024 * 1024);
	}

	public static int getVCPU(InstanceType type) {
		try {
			return Integer.parseInt(sizeconf
					.evaluateAsString("/sizings/sizing[@name='" + type
							+ "']/@vcpu"));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static InstanceType getDomainType(Domain d) {
		if (d == null) {
			return null;
		}
		float RAM = ((float) getDomainRAM(d) / 1024) / 1024;
		String sType = null;
		try {
			sType = sizeconf.evaluateAsString("/sizings/sizing[@ram='" + RAM
					+ "']/@name");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		try {
			return InstanceType.parseString(sType);
		} catch (IllegalInstanceTypeException Ex) {
			throw new RuntimeException("Unexpected error while parsing "
					+ "the InstanceType '" + sType + "'. "
					+ "Because this value have just been retreive from "
					+ "AWS, such error cannot happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	public static int getDomainVPCU(Domain d) {
		try {
			Doc doc = getDomainXMLDesc(d);
			return Integer
					.parseInt(doc.evaluateAsString("/domain/vcpu/text()"));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	/**
	 * 
	 * @param d
	 * @return the RAM quantity in Kilo Octet
	 */
	public static int getDomainRAM(Domain d) {
		try {
			Doc doc = getDomainXMLDesc(d);
			int ram = Integer.parseInt(doc
					.evaluateAsString("/domain/memory/text()"));
			String unit = doc.evaluateAsString("/domain/memory/@unit");
			if (unit.equals("GiB")) {
				return ram * 1024 * 1024;
			} else if (unit.equals("MiB")) {
				return ram * 1024;
			} else {
				return ram;
			}
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static DiskDeviceList getDiskDevices(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			DiskDeviceList dl = new DiskDeviceList();
			Doc ddoc = getDomainXMLDesc(d);
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
		PropertiesSet vars = new PropertiesSet();
		try {
			Doc ddoc = getDomainXMLDesc(d);
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
						+ "' on Domain '" + d.getName() + "' ...");
				int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
				d.detachDeviceFlags(XPathExpander.expand(
						DETACH_DISK_DEVICE_XML_SNIPPET, null, vars), flag);
				log.trace("Disk Device '" + disk.getDiskDeviceName()
						+ "' detached on Domain '" + d.getName() + "'.");
				// suppression du volume
				deleteDiskDevice(d, disk);
			}
		} catch (LibvirtException | IllegalPropertyException
				| XPathExpressionException | XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static void deleteDiskDevice(Domain d, DiskDevice disk) {
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
			String deviceToRemove = DiskDeviceNameConverter.convert(disk
					.getDiskDeviceName());
			return d.getName() + "-" + deviceToRemove + ".img";
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
			PropertiesSet vars = new PropertiesSet();
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
						+ "' for Domain '" + d.getName() + "' ...");
				StorageVol sv = sp.storageVolCreateXML(XPathExpander.expand(
						NEW_DISK_DEVICE_XML_SNIPPET, null, vars), 0);
				log.debug("Disk Device '" + disk.getDiskDeviceName()
						+ "' created for Domain '" + d.getName()
						+ "'. LibVirt Volume path is '" + sv.getPath() + "'.");

				// variabilisation de l'attachement du volume
				vars.put(new Property("volPath", sv.getPath()));
				// attachement de la device
				log.trace("Attaching Disk Device '" + disk.getDiskDeviceName()
						+ "' on Domain '" + d.getName() + "' ...");
				int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
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

	public static NetworkDeviceNameList getNetworkDevices(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			NetworkDeviceNameList ndl = new NetworkDeviceNameList();
			Doc doc = getDomainXMLDesc(d);
			NodeList nl = doc.evaluateAsNodeList("/domain/devices/interface"
					+ "[@type='network']/filterref/@filter");
			for (int i = 0; i < nl.getLength(); i++) {
				String filter = nl.item(i).getNodeValue();
				NetworkDeviceName netdev = getNetworkDeviceNameFromNetworkFilter(filter);
				ndl.addNetworkDevice(netdev);
			}
			if (ndl.size() == 0) {
				throw new RuntimeException("Failed to build Domain '"
						+ d.getName()
						+ "' Network Device List. No Network Device found ");
			}
			return ndl;
		} catch (XPathExpressionException | LibvirtException
				| IllegalNetworkDeviceNameListException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String DETACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
			+ "<mac address='§[vmMacAddr]§'/>"
			+ "<source network='default'/>"
			+ "</interface>";

	public static void detachNetworkDevice(Domain d, NetworkDeviceName netdev) {
		if (netdev == null) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}

		try {
			String sSGName = getSecurityGroup(d, netdev);
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			String netDevName = netdev.getValue();

			// Search network device @mac
			String mac = getDomainMacAddress(d, netdev);
			PropertiesSet vars = new PropertiesSet();
			vars.put(new Property("vmMacAddr", mac));

			// Detach the network device
			log.trace("Detaching Network Device '" + netDevName
					+ "' on Domain '" + sInstanceId + "' ...");
			int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
			d.detachDeviceFlags(XPathExpander.expand(
					DETACH_NETWORK_DEVICE_XML_SNIPPET, null, vars), flag);
			log.debug("Network Device '" + netDevName
					+ "' detached on Domain '" + sInstanceId + "'.");

			// Destroy the network filter
			deleteNetworkFilter(d, netdev);

			// Release the @mac
			unregisterMacAddress(mac);

			// Delete the security group
			deleteSecurityGroup(cnx, sSGName);
		} catch (XPathExpressionSyntaxException | IllegalPropertyException
				| LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String ATTACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
			+ "<mac address='§[vmMacAddr]§'/>"
			+ "<model type='virtio'/>"
			+ "<source network='default'/>"
			+ "<filterref filter='§[vmName]§-§[eth]§-nwfilter'/>"
			+ "</interface>";

	public static void attachNetworkDevice(Domain d, NetworkDeviceName netdev) {
		if (netdev == null) {
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
			String netDevName = netdev.getValue();
			// Create a security group
			String sSGName = newSecurityGroupName();
			String sSGDesc = getSecurityGroupDescription();
			createSecurityGroup(cnx, sSGName, sSGDesc);

			// Create a network filter for the network device
			PropertiesSet vars = new PropertiesSet();
			vars.put(new Property("vmMacAddr", generateUniqMacAddress()));
			vars.put(new Property("vmName", sInstanceId));
			vars.put(new Property("sgName", sSGName));
			vars.put(new Property("eth", netDevName));

			// Create a network filter for the network device
			createNetworkFilter(d, vars);

			// Attach the network device
			log.trace("Attaching Network Device '" + netDevName
					+ "' on Domain '" + sInstanceId + "' ... MacAddress is '"
					+ vars.getProperty("vmMacAddr").getValue() + "'.");
			int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
			d.attachDeviceFlags(XPathExpander.expand(
					ATTACH_NETWORK_DEVICE_XML_SNIPPET, null, vars), flag);
			log.debug("Network Device '" + netDevName
					+ "' attached on Domain '" + sInstanceId + "'.");
		} catch (XPathExpressionSyntaxException | IllegalPropertyException
				| LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static NetworkDeviceDatas getNetworkDeviceDatas(Domain d,
			NetworkDeviceName netdev) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}

		String mac = getDomainMacAddress(d, netdev);
		return new NetworkDeviceDatas(mac, getDomainIpAddress(mac),
				getDomainDnsName(mac), null, null);
	}

	public static FwRulesDecomposed getFireWallRules(Domain d,
			NetworkDeviceName netdev) {
		FwRulesDecomposed rules = new FwRulesDecomposed();
		if (netdev == null) {
			return rules;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Connect cnx = d.getConnect();
			String sSGName = getSecurityGroup(d, netdev);
			Doc doc = new Doc();
			NetworkFilter nf = cnx.networkFilterLookupByName(sSGName);
			doc.loadFromXML(nf.getXMLDesc());
			NodeList nl = doc.evaluateAsNodeList("/filter/rule");
			Node n = null;
			Interface inter = Interface.parseString(netdev.getValue());
			IpRange fromIp = null;
			IpRange toIp = null;
			PortRange fromPorts = null;
			PortRange toPorts = null;
			Protocol proto = null;
			Direction dir = null;
			Access access = null;
			for (int i = 0; i < nl.getLength(); i++) {
				n = nl.item(i);

				String sIp = XPathExpander
						.evaluateAsString("./*/@srcipaddr", n);
				String sMask = XPathExpander.evaluateAsString("./*/@srcipmask",
						n);
				fromIp = IpRange.parseString(sIp + "/" + sMask);

				String start = XPathExpander.evaluateAsString(
						"./*/@srcporstart", n);
				String end = XPathExpander.evaluateAsString("./*/@srcportend",
						n);
				fromPorts = PortRange.parseString(start + "-" + end);

				sIp = XPathExpander.evaluateAsString("./*/@dstipaddr", n);
				sMask = XPathExpander.evaluateAsString("./*/@dstipmask", n);
				toIp = IpRange.parseString(sIp + "/" + sMask);

				start = XPathExpander.evaluateAsString("./*/@dstportstart", n);
				end = XPathExpander.evaluateAsString("./*/@dstportend", n);
				toPorts = PortRange.parseString(start + "-" + end);

				String sProtocol = XPathExpander.evaluateAsString(
						"./node-name(*)", n);
				if (sProtocol.equalsIgnoreCase("tcp")) {
					proto = Protocol.TCP;
				} else {
					proto = Protocol.UDP;
				}// TODO : handle ICMP

				String sDirection = XPathExpander.evaluateAsString(
						"./@direction", n);
				if (sDirection.equalsIgnoreCase("in")) {
					dir = Direction.IN;
				} else {
					dir = Direction.OUT;
				}

				String sAccess = XPathExpander.evaluateAsString("./@action", n);
				if (sAccess.equalsIgnoreCase("accept")) {
					access = Access.ALLOW;
				} else {
					access = Access.DENY;
				}

				rules.add(new FwRuleDecomposed(inter, fromIp, fromPorts, toIp,
						toPorts, proto, dir, access));
			}
		} catch (LibvirtException | MelodyException | IOException
				| XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		return rules;
	}

	public static void revokeFireWallRules(Domain d, NetworkDeviceName netdev,
			FwRulesDecomposed rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			String sInstanceId = d.getName();
			Connect cnx = d.getConnect();
			String sSGName = getSecurityGroup(d, netdev);
			Doc doc = new Doc();
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			doc.loadFromXML(sg.getXMLDesc());

			for (FwRuleDecomposed rule : rules) {
				// TODO : handle ICMP
				Node n = doc
						.evaluateAsNode("/filter/rule[" + " @action='"
								+ (rule.getAccess() == Access.ALLOW ? "accept"
										: "drop")
								+ "' and @direction='"
								+ (rule.getDirection() == Direction.IN ? "in"
										: "out")
								+ "' and exists("
								+ (rule.getProtocol() == Protocol.TCP ? "tcp"
										: "udp") + "[" + "@srcipaddr='"
								+ rule.getFromIpRange().getIp()
								+ "' and @srcipmask='"
								+ rule.getFromIpRange().getMask()
								+ "' and @srcportstart='"
								+ rule.getFromPortRange().getStartPort()
								+ "' and @srcportend='"
								+ rule.getFromPortRange().getEndPort()
								+ "' and @dstipaddr='"
								+ rule.getToIpRange().getIp()
								+ "' and @dstipmask='"
								+ rule.getToIpRange().getMask()
								+ "' and @dstportstart='"
								+ rule.getToPortRange().getStartPort()
								+ "' and @dstportend='"
								+ rule.getToPortRange().getEndPort() + "'])]");
				if (n != null) {
					n.getParentNode().removeChild(n);
					log.debug("Domain '" + sInstanceId + "' revokes '" + netdev
							+ "' the FireWall rule " + rule + ".");
				}
			}

			String dump = doc.dump();
			cnx.networkFilterDefineXML(dump);
		} catch (LibvirtException | MelodyException | IOException
				| XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void authorizeFireWallRules(Domain d,
			NetworkDeviceName netdev, FwRulesDecomposed rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			String sInstanceId = d.getName();
			Connect cnx = d.getConnect();
			String sSGName = getSecurityGroup(d, netdev);
			Doc doc = new Doc();
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			doc.loadFromXML(sg.getXMLDesc());

			for (FwRuleDecomposed rule : rules) {
				// TODO : handle ICMP
				Node nrule = doc.getDocument().createElement("rule");
				Doc.createAttribute("priority", "500", nrule);
				Doc.createAttribute("action",
						rule.getAccess() == Access.ALLOW ? "accept" : "drop",
						nrule);
				Doc.createAttribute("direction",
						rule.getDirection() == Direction.IN ? "in" : "out",
						nrule);

				Node nin = doc.getDocument().createElement(
						rule.getProtocol() == Protocol.TCP ? "tcp" : "udp");
				nrule.appendChild(nin);
				Doc.createAttribute("state", "NEW", nin);

				Doc.createAttribute("srcipaddr", rule.getFromIpRange().getIp(),
						nin);
				Doc.createAttribute("srcipmask", rule.getFromIpRange()
						.getMask(), nin);

				Doc.createAttribute("srcportstart", rule.getFromPortRange()
						.getStartPort().toString(), nin);
				Doc.createAttribute("srcportend", rule.getFromPortRange()
						.getEndPort().toString(), nin);

				Doc.createAttribute("dstipaddr", rule.getToIpRange().getIp(),
						nin);
				Doc.createAttribute("dstipmask", rule.getToIpRange().getMask(),
						nin);

				Doc.createAttribute("dstportstart", rule.getToPortRange()
						.getStartPort().toString(), nin);
				Doc.createAttribute("dstportend", rule.getToPortRange()
						.getEndPort().toString(), nin);

				doc.getDocument().getFirstChild().appendChild(nrule);
				log.debug("Domain '" + sInstanceId + "' grants '" + netdev
						+ "' the FireWall rule " + rule + ".");
			}

			String dump = doc.dump();
			cnx.networkFilterDefineXML(dump);
		} catch (LibvirtException | MelodyException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Path getImageDomainDescriptor(String sImageId) {
		String sPath = null;
		try {
			sPath = conf.evaluateAsString("//images/image[@name='" + sImageId
					+ "']/@descriptor");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.");
		}
		return Paths.get(sPath);
	}

	private static String generateUniqDomainName(Connect cnx) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		String sNewName = null;
		while (true) {
			sNewName = "i-" + UUID.randomUUID().toString().substring(0, 8);
			if (!instanceExists(cnx, sNewName)) {
				return sNewName;
			}
		}
	}

	private static synchronized String generateUniqMacAddress() {
		NodeList nlFreeMacAddrPool = null;
		try {
			nlFreeMacAddrPool = netconf.evaluateAsNodeList("/network/ip/dhcp"
					+ "/host[ not(exists(@allocated)) or @allocated!='true' ]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (nlFreeMacAddrPool == null || nlFreeMacAddrPool.getLength() == 0) {
			throw new RuntimeException("No more free Mac Address.");
		}
		String sFirstFreeMacAddr = nlFreeMacAddrPool.item(0).getAttributes()
				.getNamedItem("mac").getNodeValue();
		log.trace("Allocating Mac Address '" + sFirstFreeMacAddr + "' ...");
		Doc.createAttribute("allocated", "true", nlFreeMacAddrPool.item(0));
		netconf.store();
		log.debug("Mac Address '" + sFirstFreeMacAddr + "' allocated.");
		return sFirstFreeMacAddr;
	}

	private static synchronized void unregisterMacAddress(String sMacAddr) {
		if (sMacAddr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		log.trace("Releasing Mac Address '" + sMacAddr + "' ...");
		Node nMacAddr = null;
		try {
			nMacAddr = netconf.evaluateAsNode("/network/ip/dhcp" + "/host"
					+ "[ upper-case(@mac)=upper-case('" + sMacAddr
					+ "') and exists(@allocated) ]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (nMacAddr == null) {
			return;
		}
		nMacAddr.getAttributes().removeNamedItem("allocated");
		netconf.store();
		log.debug("Mac Address '" + sMacAddr + "' released.");
	}

	protected static Doc getDomainXMLDesc(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Doc doc = new Doc();
			doc.loadFromXML(d.getXMLDesc(0));
			return doc;
		} catch (MelodyException | LibvirtException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String getDomainMacAddress(Domain d, NetworkDeviceName netdev) {
		if (netdev == null) {
			netdev = eth0;
		}
		try {
			Doc doc = getDomainXMLDesc(d);
			return doc.evaluateAsString("/domain/devices/interface"
					+ "[@type='network' and filterref/@filter='"
					+ getNetworkFilter(d, netdev) + "']/mac/@address");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static String getDomainIpAddress(String sMacAddr) {
		if (sMacAddr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			return netconf.evaluateAsString("/network/ip/dhcp"
					+ "/host[ upper-case(@mac)=upper-case('" + sMacAddr
					+ "') ]/@ip");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.");
		}
	}

	protected static String getDomainDnsName(String sMacAddr) {
		if (sMacAddr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			return netconf.evaluateAsString("/network/ip/dhcp"
					+ "/host[ upper-case(@mac)=upper-case('" + sMacAddr
					+ "') ]/@name");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.");
		}
	}

	public static boolean imageIdExists(String sImageId) {
		if (sImageId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		try {
			return null != conf.evaluateAsNode("//images/image[@name='"
					+ sImageId + "']");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.", Ex);
		}
	}

	public static boolean networkFilterExists(Connect cnx, String sSGName)
			throws LibvirtException {
		if (sSGName == null) {
			return false;
		}
		String[] names = cnx.listNetworkFilters();
		return Arrays.asList(names).contains(sSGName);
	}

	public static Domain getDomain(Connect cnx, String sInstanceId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		if (sInstanceId == null) {
			return null;
		}
		try {
			return cnx.domainLookupByName(sInstanceId);
		} catch (LibvirtException Ex) {
			if (Ex.getError().getCode() == ErrorNumber.VIR_ERR_NO_DOMAIN) {
				return null;
			}
			throw new RuntimeException(Ex);
		}
	}

	public static boolean instanceExists(Connect cnx, String sInstanceId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		if (sInstanceId == null) {
			return false;
		}
		try {
			String[] names = cnx.listDefinedDomains();
			return Arrays.asList(names).contains(sInstanceId);
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static InstanceState getDomainState(Domain d) {
		if (d == null) {
			return null;
		}
		DomainState state = null;
		try {
			state = d.getInfo().state;
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
		try {
			return InstanceStateConverter.parse(state);
		} catch (IllegalInstanceStateException Ex) {
			throw new RuntimeException("Unexpected error while creating an "
					+ "InstanceState Enum based on the value '" + state + "'. "
					+ "Because this value was given by the LibVirt API, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public static InstanceState getInstanceState(Connect cnx, String sInstanceId) {
		Domain d = getDomain(cnx, sInstanceId);
		return getDomainState(d);
	}

	public static boolean instanceLives(Connect cnx, String sInstanceId) {
		InstanceState cs = getInstanceState(cnx, sInstanceId);
		if (cs == null) {
			return false;
		}
		return cs != InstanceState.SHUTTING_DOWN
				&& cs != InstanceState.TERMINATED;
	}

	public static boolean instanceRuns(Connect cnx, String sInstanceId) {
		InstanceState cs = getInstanceState(cnx, sInstanceId);
		if (cs == null) {
			return false;
		}
		return cs == InstanceState.PENDING || cs == InstanceState.RUNNING;
	}

	/**
	 * <p>
	 * Wait until an Instance reaches the given state.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the requested Instance doesn't exist, this call return
	 * <code>false</code> after all the timeout elapsed. <BR/>
	 * * If the given timeout is equal to 0, this call will wait forever. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param cnx
	 * @param sInstanceId
	 *            is the requested Instance Identifier.
	 * @param state
	 *            is the state to reach.
	 * @param timeout
	 *            is the maximal amount of time to wait for the requested
	 *            Instance to reach the given state, in millis.
	 * @param sleepfirst
	 *            is an extra initial amount of time to wait, in millis.
	 * 
	 * @return <code>true</code> if the requested Instance reaches the given
	 *         state before the given timeout expires, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 * @throws IllegalArgumentException
	 *             if cnx is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws IllegalArgumentException
	 *             if sleepfirst is a negative long.
	 */
	public static boolean waitUntilInstanceStatusBecomes(Connect cnx,
			String sInstanceId, InstanceState state, long timeout,
			long sleepfirst) throws InterruptedException {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (sInstanceId == null || sInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sInstanceId + ": Not accepted. "
					+ "Must be a String (an Instance Id).");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}
		if (sleepfirst < 0) {
			throw new IllegalArgumentException(sleepfirst + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;

		Thread.sleep(sleepfirst);
		InstanceState is = null;
		while ((is = getInstanceState(cnx, sInstanceId)) != state) {
			log.debug("Domain for Instance '" + sInstanceId + "' to become '"
					+ state + "'. Currently '" + is + "'.");
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn("Domain '" + sInstanceId + "' is still not '" + state
						+ "' after " + timeout + " seconds.");
				return false;
			}
		}
		log.info("Domain '" + sInstanceId + "' reaches the state '" + state
				+ "' in " + (System.currentTimeMillis() - start) / 1000
				+ " seconds.");
		return true;
	}

	/*
	 * A dedicated Network Filter is associated to each network device of each
	 * Domain. The Network filter name is
	 * '<instance-id>-<device-name>-nwfilter'.
	 * 
	 * This Network Filter is the placeholder of 'common' firewall rules (e.g.
	 * which apply to all network device).
	 * 
	 * The first element of the Network Filter must be a filterref, which points
	 * to the Security Group of the network device.
	 * 
	 * A security Group is the placeholder of firewall rules which are specific
	 * to the network device. The Security Group name is 'MelodySg_<13 ramdon
	 * number>_<8 random digit>'.
	 */
	public static String getNetworkFilter(Domain d, NetworkDeviceName netdev) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			return d.getName() + "-" + netdev.getValue() + "-nwfilter";
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static NetworkDeviceName getNetworkDeviceNameFromNetworkFilter(
			String filter) {
		filter = filter.substring(0, filter.lastIndexOf('-'));
		filter = filter.substring(filter.lastIndexOf('-') + 1);
		try {
			return NetworkDeviceName.parseString(filter);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static void createNetworkFilter(Domain d, PropertiesSet ps) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			String sSGName = ps.get("sgName");
			String eth = ps.get("eth");
			log.trace("Creating Network Filter '" + sInstanceId + "-" + eth
					+ "-nwfilter' (linked to Security Group '" + sSGName
					+ "') for Domain '" + sInstanceId + "' ...");
			cnx.networkFilterDefineXML(XPathExpander.expand(
					DOMAIN_NETWORK_FILTER_XML_SNIPPET, null, ps));
			log.debug("Network Filter '" + sInstanceId + "-" + eth
					+ "-nwfilter' (linked to Security Group '" + sSGName
					+ "') created for Domain '" + sInstanceId + "'.");
		} catch (LibvirtException | XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static void deleteNetworkFilter(Domain d, NetworkDeviceName netdev) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			String filter = getNetworkFilter(d, netdev);
			log.trace("Deleting Network Filter '" + filter + "' for Domain '"
					+ sInstanceId + "' ...");
			NetworkFilter nf = cnx.networkFilterLookupByName(filter);
			nf.undefine();
			log.debug("Network Filter '" + filter + "' deleted for Domain '"
					+ sInstanceId + "'.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String getSecurityGroup(Domain d, NetworkDeviceName netdev) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			String filter = getNetworkFilter(d, netdev);
			if (!networkFilterExists(d.getConnect(), filter)) {
				return null;
			}
			NetworkFilter nf = d.getConnect().networkFilterLookupByName(filter);
			Doc doc = new Doc();
			doc.loadFromXML(nf.getXMLDesc());
			return doc.evaluateAsString("//filterref[1]/@filter");
		} catch (MelodyException | XPathExpressionException | LibvirtException
				| IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void createSecurityGroup(Connect cnx, String sSGName,
			String sSGDesc) {
		// Create a network filter for the network device
		try {
			if (networkFilterExists(cnx, sSGName)) {
				throw new RuntimeException(sSGName + ": network filter "
						+ "already exists.");
			}
			String NETWORK_FILTER_XML_SNIPPET = "<filter name='" + sSGName
					+ "' chain='root'>" + "</filter>";
			log.trace("Creating Security Group '" + sSGName + "' ...");
			cnx.networkFilterDefineXML(NETWORK_FILTER_XML_SNIPPET);
			log.debug("Security Group '" + sSGName + "' created.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void deleteSecurityGroup(Connect cnx, String sSGName) {
		try {
			if (!networkFilterExists(cnx, sSGName)) {
				return;
			}
			log.trace("Deleting Security Group '" + sSGName + "' ...");
			NetworkFilter nf = cnx.networkFilterLookupByName(sSGName);
			nf.undefine();
			log.debug("Security Group '" + sSGName + "' deleted.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void deleteSecurityGroups(Connect cnx,
			Map<NetworkDeviceName, String> sgs) {
		for (String sg : sgs.values()) {
			deleteSecurityGroup(cnx, sg);
		}
	}

	private static String getSecurityGroupDescription() {
		return "Melody security group";
	}

	private static String newSecurityGroupName() {
		// This formula should produce a unique name
		return "MelodySg" + "_" + System.currentTimeMillis() + "_"
				+ UUID.randomUUID().toString().substring(0, 8);
	}

	private static String DOMAIN_NETWORK_FILTER_XML_SNIPPET = "<filter name='§[vmName]§-§[eth]§-nwfilter' chain='root'>"
			+ "<filterref filter='§[sgName]§'/>"
			// clean-traffic will drop packets sent on eth1 and reply on eth0
			// + "<filterref filter='clean-traffic'/>"
			+ "<rule action='accept' direction='out' priority='500'>"
			+ "<all state='NEW'/>"
			+ "</rule>"
			+ "<rule action='accept' direction='out' priority='500'>"
			+ "<all state='ESTABLISHED,RELATED'/>"
			+ "</rule>"
			+ "<rule action='accept' direction='in' priority='500'>"
			+ "<all state='ESTABLISHED'/>"
			+ "</rule>"
			+ "<rule action='drop' direction='inout' priority='500'>"
			+ "<all/>" + "</rule>" + "</filter>";

	private static String LOCK_UNIQ_DOMAIN = "";
	private static String LOCK_CLONE_DISK = "";
	private static NetworkDeviceName eth0 = createNetworkDeviceName("eth0");

	private static NetworkDeviceName createNetworkDeviceName(String n) {
		try {
			return NetworkDeviceName.parseString(n);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String newInstance(Connect cnx, InstanceType type,
			String sImageId, KeyPairName keyPairName) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}

		/*
		 * TODO : should be asynchronous.
		 * 
		 * Should handle a PENDING state.
		 */
		try {
			if (!imageIdExists(sImageId)) {
				throw new RuntimeException(sImageId + ": No such image.");
			}

			// Create a dedicated security group
			String sSGName = newSecurityGroupName();
			String sSGDesc = getSecurityGroupDescription();
			createSecurityGroup(cnx, sSGName, sSGDesc);

			Path ddt = getImageDomainDescriptor(sImageId);
			PropertiesSet ps = new PropertiesSet();
			Domain domain = null;
			String sInstanceId = null;
			// Defines domain
			synchronized (LOCK_UNIQ_DOMAIN) {
				// this block is sync because the sInstanceId must be consistent
				sInstanceId = generateUniqDomainName(cnx);
				ps.put(new Property("vmName", sInstanceId));
				ps.put(new Property("vmMacAddr", generateUniqMacAddress()));
				ps.put(new Property("vcpu", String.valueOf(getVCPU(type))));
				ps.put(new Property("ram", String.valueOf(getRAM(type))));
				ps.put(new Property("sgName", sSGName));
				ps.put(new Property("eth", eth0.getValue()));
				log.trace("Creating domain '" + sInstanceId
						+ "' based on the template " + sImageId
						+ " ... MacAddress is '"
						+ ps.getProperty("vmMacAddr").getValue() + "'.");
				domain = cnx.domainDefineXML(XPathExpander
						.expand(ddt, null, ps));
			}
			log.debug("Domain '" + sInstanceId + "' created.");

			// Create a network filter for the network device
			createNetworkFilter(domain, ps);

			// Create disk devices
			NodeList nl = null;
			nl = conf.evaluateAsNodeList("//images/image[@name='" + sImageId
					+ "']/disk");
			// recuperation du storage pool
			StoragePool sp = cnx.storagePoolLookupByName("default");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				Node descriptor = n.getAttributes().getNamedItem("descriptor");
				Node source = n.getAttributes().getNamedItem("source");
				Node device = n.getAttributes().getNamedItem("device");
				String sDescriptorPath = descriptor.getNodeValue();
				String sSourceVolumePath = source.getNodeValue();
				String sDiskDeviceName = device.getNodeValue();
				log.trace("Creating Disk Device '" + sDiskDeviceName
						+ "' for Domain '" + sInstanceId
						+ "' ... LibVirt Volume Image is '" + sSourceVolumePath
						+ "'.");
				StorageVol sourceVolume = cnx
						.storageVolLookupByPath(sSourceVolumePath);
				String sDescriptor = null;
				sDescriptor = XPathExpander.expand(Paths.get(sDescriptorPath),
						null, ps);
				StorageVol sv = null;
				synchronized (LOCK_CLONE_DISK) {
					// we can't clone a volume which is already being cloned
					// and we can't use nio.Files.copy which is 4 times slower
					sv = sp.storageVolCreateXMLFrom(sDescriptor, sourceVolume,
							0);
				}
				log.debug("Disk Device '" + sDiskDeviceName
						+ "' created for Domain '" + sInstanceId
						+ "'. LibVirt Volume path is '" + sv.getPath() + "'.");
			}

			// Starts domain
			log.trace("Starting Domain '" + sInstanceId + "' ...");
			domain.create();
			log.debug("Domain '" + sInstanceId + "' started.");
			return sInstanceId;
		} catch (LibvirtException | MelodyException | IOException
				| XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void deleteInstance(Domain d) {
		if (d == null) {
			return;
		}
		/*
		 * TODO : should be asynchronous.
		 * 
		 * Should shutdown (instead of destroy) the domain. Should handle a
		 * SHUTTING_DOWN and a TERMINATED state.
		 */
		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			DomainState state = d.getInfo().state;
			// Destroy domain
			if (state == DomainState.VIR_DOMAIN_RUNNING
					|| state == DomainState.VIR_DOMAIN_PAUSED) {
				log.trace("Destroying Domain '" + sInstanceId + "' ...");
				d.destroy();
				log.debug("Domain '" + sInstanceId + "' destroyed.");
			}
			// Release network devices
			NetworkDeviceNameList netdevs = getNetworkDevices(d);
			for (NetworkDeviceName netdev : netdevs) {
				String sSGName = getSecurityGroup(d, netdev);
				// Destroy the network filter
				deleteNetworkFilter(d, netdev);
				// Release the @mac
				String mac = getDomainMacAddress(d, netdev);
				unregisterMacAddress(mac);
				// Destroy the security group
				deleteSecurityGroup(cnx, sSGName);
			}
			// Destroy disk devices
			DiskDeviceList diskdevs = getDiskDevices(d);
			for (DiskDevice disk : diskdevs) {
				// Destroy disk device
				deleteDiskDevice(d, disk);
			}
			// Undefine domain
			d.undefine();
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static boolean startInstance(Domain d, long startTimeout)
			throws InterruptedException {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			String sInstanceId = d.getName();
			// Start domain
			log.trace("Starting Domain '" + sInstanceId + "' ...");
			d.create();
			// Wait for the Domain to start
			if (!waitUntilInstanceStatusBecomes(d.getConnect(), sInstanceId,
					InstanceState.RUNNING, startTimeout, 5000)) {
				return false;
			}
			log.debug("Domain '" + sInstanceId + "' started.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
		return true;
	}

	public static boolean stopInstance(Domain d, long stopTimeout)
			throws InterruptedException {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			// Stop Domain
			String sInstanceId = d.getName();
			log.trace("Stopping Domain '" + sInstanceId + "' ...");
			d.shutdown();
			// Wait for the Domain to stop
			if (!waitUntilInstanceStatusBecomes(d.getConnect(), sInstanceId,
					InstanceState.STOPPED, stopTimeout, 5000)) {
				return false;
			}
			log.debug("Domain '" + sInstanceId + "' stopped.");
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
		return true;
	}

	public static boolean resizeInstance(Domain d, InstanceType targetType)
			throws InterruptedException {
		try {
			/*
			 * Memory hotplug/unplug is not for real (it acts on balloning), and
			 * cpu unplug is not supported. For these reason, we only deal with
			 * 'stopped' domain.
			 */
			if (getDomainState(d) != InstanceState.STOPPED) {
				return false;
			}
			String sMemory = sizeconf.evaluateAsString("//sizing[@name='"
					+ targetType + "']/@ram");
			String sCPU = sizeconf.evaluateAsString("//sizing[@name='"
					+ targetType + "']/@vcpu");

			String sTargetMemory = String.valueOf((long) (Float
					.parseFloat(sMemory) * 1024 * 1024));

			/*
			 * we cannot use Domin.setMemory and Domain.setVcpus, because it
			 * only work on 'running ' domain.
			 */
			Doc doc = getDomainXMLDesc(d);
			Node node;
			node = doc.evaluateAsNode("/domain/currentMemory");
			node.setTextContent(sTargetMemory);
			node = doc.evaluateAsNode("/domain/memory");
			node.setTextContent(sTargetMemory);
			node = doc.evaluateAsNode("/domain/vcpu");
			node.setTextContent(sCPU);

			d.getConnect().domainDefineXML(doc.dump());
		} catch (XPathExpressionException | NumberFormatException
				| LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
		return true;
	}

}