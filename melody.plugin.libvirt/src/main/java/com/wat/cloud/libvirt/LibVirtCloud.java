package com.wat.cloud.libvirt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceException;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.plugin.libvirt.common.InstanceStateConverter;
import com.wat.melody.xpathextensions.XPathExpander;
import com.wat.melody.xpathextensions.common.exception.XPathExpressionSyntaxException;

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

	public static int getCore(InstanceType type) {
		try {
			return Integer.parseInt(sizeconf
					.evaluateAsString("/sizings/sizing[@name='" + type
							+ "']/@core"));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static int getSocket(InstanceType type) {
		try {
			return Integer.parseInt(sizeconf
					.evaluateAsString("/sizings/sizing[@name='" + type
							+ "']/@socket"));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static int getVCPU(InstanceType type) {
		return getCore(type) * getSocket(type);
	}

	protected static InstanceType getDomainType(Domain d) {
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
			throw new RuntimeException(sizeconf.getFileFullPath()
					+ ": instance name '" + sType
					+ "' is not a valid instance type. "
					+ "Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ".");
		}
	}

	public static int getDomainVPCU(Domain domain) {
		try {
			Doc doc = getDomainXMLDesc(domain);
			return Integer
					.parseInt(doc.evaluateAsString("/domain/vcpu/text()"));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	/**
	 * 
	 * @param domain
	 * @return the RAM quantity in Kilo Octet
	 */
	public static int getDomainRAM(Domain domain) {
		try {
			Doc doc = getDomainXMLDesc(domain);
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

	public static DiskDeviceList getInstanceDiskDevices(Instance i) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		return getDomainDiskDevices(i.getDomain());
	}

	public static DiskDeviceList getDomainDiskDevices(Domain domain) {
		try {
			DiskDeviceList dl = new DiskDeviceList();
			Doc ddoc = getDomainXMLDesc(domain);
			NodeList nl = ddoc
					.evaluateAsNodeList("/domain/devices/disk[@device='disk']");
			for (int i = 0; i < nl.getLength(); i++) {
				DiskDevice d = new DiskDevice();
				String volPath = Doc.evaluateAsString("source/@file",
						nl.item(i));
				StorageVol sv = domain.getConnect().storageVolLookupByPath(
						volPath);
				d.setDeleteOnTermination(true);
				d.setDeviceName("/dev/"
						+ Doc.evaluateAsString("target/@dev", nl.item(i)));
				d.setSize((int) (sv.getInfo().capacity / (1024 * 1024 * 1024)));
				if (d.getDeviceName().equals("/dev/vda")) {
					d.setRootDevice(true);
				}
				dl.addDiskDevice(d);
			}
			if (dl.size() == 0) {
				throw new RuntimeException("Failed to build Domain '"
						+ domain.getName()
						+ "' Disk Device List. No Disk Device found ");
			}
			if (dl.getRootDevice() == null) {
				throw new RuntimeException("Failed to build Domain '"
						+ domain.getName()
						+ "' Disk Device List. No Root Disk Device found ");
			}
			return dl;
		} catch (XPathExpressionException | IllegalDiskDeviceException
				| LibvirtException | IllegalDiskDeviceListException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String DEL_DISK_DEVICE_XML_SNIPPET = "<volume>"
			+ "<name>§[vmName]§-vol§[volNum]§.img</name>" + "<source>"
			+ "</source>" + "<capacity unit='bytes'>§[capacity]§</capacity>"
			+ "<allocation unit='bytes'>§[allocation]§</allocation>"
			+ "<target>" + "<format type='raw'/>" + "</target>" + "</volume>";

	public static final String DETACH_DISK_DEVICE_XML_SNIPPET = "<disk type='file' device='disk'>"
			+ "<source file='§[volPath]§'/>"
			+ "<target dev='§[targetDevice]§' bus='virtio'/>" + "</disk>";

	public static void detachAndDeleteDiskDevices(Instance i,
			DiskDeviceList disksToRemove) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		detachAndDeleteDiskDevices(i.getDomain(), disksToRemove);
	}

	public static void detachAndDeleteDiskDevices(Domain d,
			DiskDeviceList disksToRemove) {
		if (disksToRemove == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (disksToRemove.size() == 0) {
			return;
		}

		// preparation de la variabilisation du XML
		PropertiesSet vars = new PropertiesSet();
		try {
			Doc ddoc = getDomainXMLDesc(d);
			// pour chaque disque a supprimer
			for (DiskDevice disk : disksToRemove) {
				String deviceToRemove = disk.getDeviceName().replace("/dev/",
						"");
				// search the path of the disk which match device to remove
				String volPath = ddoc
						.evaluateAsString("/domain/devices/disk[@device='disk' and target/@dev='"
								+ deviceToRemove + "']/source/@file");
				if (volPath == null) {
					throw new RuntimeException("Domain '" + d.getName()
							+ "' has no disk device '" + deviceToRemove + "' !");
				}
				// variabilisation du detachement de la device
				vars.put(new Property("targetDevice", deviceToRemove));
				vars.put(new Property("volPath", volPath));
				// detachement de la device
				log.trace("Detaching Disk Device '" + disk.getDeviceName()
						+ "' on Domain '" + d.getName() + "' ...");
				int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
				d.detachDeviceFlags(XPathExpander.expand(
						DETACH_DISK_DEVICE_XML_SNIPPET, null, vars), flag);
				log.trace("Disk Device '" + disk.getDeviceName()
						+ "' detached on Domain '" + d.getName() + "'.");
				// suppression du volume
				StorageVol sv = d.getConnect().storageVolLookupByPath(volPath);
				log.trace("Deleting Disk Device '" + disk.getDeviceName()
						+ "' on domain '" + d.getName()
						+ "' ... LibVirt Volume path is '" + sv.getPath()
						+ "'.");
				sv.delete(0);
				log.debug("Disk Device '" + disk.getDeviceName()
						+ "' deleted on Domain '" + d.getName() + "'.");
			}
		} catch (LibvirtException | IllegalPropertyException
				| XPathExpressionException | XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String NEW_DISK_DEVICE_XML_SNIPPET = "<volume>"
			+ "<name>§[vmName]§-vol§[volNum]§.img</name>" + "<source>"
			+ "</source>" + "<capacity unit='bytes'>§[capacity]§</capacity>"
			+ "<allocation unit='bytes'>§[allocation]§</allocation>"
			+ "<target>" + "<format type='raw'/>" + "</target>" + "</volume>";

	public static final String ATTACH_DISK_DEVICE_XML_SNIPPET = "<disk type='file' device='disk'>"
			+ "<driver name='qemu' type='raw' cache='none'/>"
			+ "<source file='§[volPath]§'/>"
			+ "<target dev='§[targetDevice]§' bus='virtio'/>" + "</disk>";

	public static void createAndAttachDiskDevices(Instance i,
			DiskDeviceList disksToAdd) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		createAndAttachDiskDevices(i.getDomain(), disksToAdd);
	}

	public static void createAndAttachDiskDevices(Domain d,
			DiskDeviceList disksToAdd) {
		if (disksToAdd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (disksToAdd.size() == 0) {
			return;
		}

		try {
			Doc ddoc = getDomainXMLDesc(d);
			// Obtenir l'indice de volume le plus eleve
			NodeList nl = ddoc
					.evaluateAsNodeList("/domain/devices/disk[@device='disk']/source/@file");
			if (nl.getLength() == 0) {
				throw new RuntimeException("Domain '" + d.getName()
						+ "' has no Disk Device !");
			}
			int lastVol = 0;
			Pattern p2 = Pattern
					.compile("^/.*/i-[a-zA-Z0-9]{8}-vol([0-9]+)[.]img$");
			for (int i = 0; i < nl.getLength(); i++) {
				Matcher m = p2.matcher(nl.item(i).getNodeValue());
				if (!m.matches()) {
					throw new RuntimeException("Domain '" + d.getName()
							+ "' Disk Device Name '"
							+ nl.item(i).getNodeValue()
							+ "' doesn't match Disk Device Name pattern !");
				}
				int curVol = Integer.parseInt(m.group(1));
				if (curVol > lastVol) {
					lastVol = curVol;
				}
			}
			// recuperation du storage pool
			StoragePool sp = d.getConnect().storagePoolLookupByName("default");
			// preparation de la variabilisation des template XML
			PropertiesSet vars = new PropertiesSet();
			vars.put(new Property("vmName", d.getName()));
			vars.put(new Property("allocation", "0"));
			// pour chaque disque
			for (DiskDevice disk : disksToAdd) {
				// variabilisation de la creation du volume
				vars.put(new Property("volNum", String.valueOf(++lastVol)));
				vars.put(new Property("capacity", String.valueOf((long) disk
						.getSize() * 1024 * 1024 * 1024)));
				// creation du volume
				log.trace("Creating Disk Device '" + disk.getDeviceName()
						+ "' for Domain '" + d.getName() + "' ...");
				StorageVol sv = sp.storageVolCreateXML(XPathExpander.expand(
						NEW_DISK_DEVICE_XML_SNIPPET, null, vars), 0);
				log.debug("Disk Device '" + disk.getDeviceName()
						+ "' created for Domain '" + d.getName()
						+ "'. LibVirt Volume path is '" + sv.getPath() + "'.");

				// variabilisation de l'attachement du volume
				String deviceToAdd = disk.getDeviceName().replace("/dev/", "");
				vars.put(new Property("targetDevice", deviceToAdd));
				vars.put(new Property("volPath", sv.getPath()));
				// attachement de la device
				log.trace("Attaching Disk Device '" + disk.getDeviceName()
						+ "' on Domain '" + d.getName() + "' ...");
				int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
				d.attachDeviceFlags(XPathExpander.expand(
						ATTACH_DISK_DEVICE_XML_SNIPPET, null, vars), flag);
				log.debug("Disk Device '" + disk.getDeviceName()
						+ "' attached on Domain '" + d.getName() + "'.");
			}
		} catch (LibvirtException | IllegalPropertyException
				| XPathExpressionException | XPathExpressionSyntaxException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static NetworkDeviceList getInstanceNetworkDevices(Instance i) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		return getDomainNetworkDevices(i.getDomain());
	}

	public static NetworkDeviceList getDomainNetworkDevices(Domain domain) {
		try {
			NetworkDeviceList ndl = new NetworkDeviceList();
			Doc ddoc = getDomainXMLDesc(domain);
			NodeList nl = ddoc
					.evaluateAsNodeList("/domain/devices/interface[@type='network']");
			for (int i = 0; i < nl.getLength(); i++) {
				NetworkDevice d = new NetworkDevice();
				d.setDeviceName("eth" + String.valueOf(i));
				ndl.addNetworkDevice(d);
			}
			if (ndl.size() == 0) {
				throw new RuntimeException("Failed to build Domain '"
						+ domain.getName()
						+ "' Network Device List. No Network Device found ");
			}
			return ndl;
		} catch (XPathExpressionException | IllegalNetworkDeviceException
				| LibvirtException | IllegalNetworkDeviceListException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String DETACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
			+ "<mac address='§[vmMacAddr]§'/>"
			+ "<source network='default'/>"
			+ "</interface>";

	public static void detachNetworkDevice(Instance i, NetworkDevice netDev) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		detachNetworkDevice(i.getDomain(), netDev);
	}

	public static void detachNetworkDevice(Domain d, NetworkDevice netDev) {
		if (netDev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDevice.class.getCanonicalName() + ".");
		}

		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			String netDevName = netDev.getDeviceName();
			Doc doc = getDomainXMLDesc(d);
			Node n = doc.evaluateAsNode("/domain/devices/interface"
					+ "[@type='network' and alias/@name='net"
					+ netDevName.substring(3) + "']");
			String mac = Doc.evaluateAsString("./mac/@address", n);
			PropertiesSet vars = new PropertiesSet();
			vars.put(new Property("vmMacAddr", mac));

			// Detach the network device
			log.trace("Detaching Network Device '" + netDevName
					+ "' on Domain '" + sInstanceId + "' ...");
			// detachement de la device
			int flag = getDomainState(d) == InstanceState.RUNNING ? 3 : 2;
			d.detachDeviceFlags(XPathExpander.expand(
					DETACH_NETWORK_DEVICE_XML_SNIPPET, null, vars), flag);
			log.debug("Network Device '" + netDevName
					+ "' detached on Domain '" + sInstanceId + "'.");

			// Destroy the network filter
			String filter = Doc.evaluateAsString("./filterref/@filter", n);
			if (networkFilterExists(cnx, filter)) {
				log.trace("Deleting Network Filter '" + filter
						+ "' for Domain '" + sInstanceId + "' ...");
				NetworkFilter nf = cnx.networkFilterLookupByName(filter);
				nf.undefine();
				log.debug("Network Filter '" + filter
						+ "' deleted for Domain '" + sInstanceId + "'.");
			}

			// Release the @mac
			unregisterMacAddress(mac);
		} catch (XPathExpressionSyntaxException | IllegalPropertyException
				| LibvirtException | XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static final String ATTACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
			+ "<mac address='§[vmMacAddr]§'/>"
			+ "<model type='virtio'/>"
			+ "<source network='default'/>"
			+ "<filterref filter='§[vmName]§-§[eth]§-nwfilter'/>"
			+ "</interface>";

	public static void attachNetworkDevice(Instance i, NetworkDevice netDev,
			String sSGName) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		attachNetworkDevice(i.getDomain(), netDev, sSGName);
	}

	public static void attachNetworkDevice(Domain d, NetworkDevice netDev,
			String sSGName) {
		if (netDev == null) {
			return;
		}
		if (sSGName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName());
		}

		try {
			Connect cnx = d.getConnect();
			String sInstanceId = d.getName();
			String netDevName = netDev.getDeviceName();
			PropertiesSet vars = new PropertiesSet();
			vars.put(new Property("vmMacAddr", generateUniqMacAddress()));
			vars.put(new Property("vmName", sInstanceId));
			vars.put(new Property("sgName", sSGName));
			vars.put(new Property("eth", netDevName));

			// Create a network filter for the network device
			String filter = sInstanceId + "-" + netDevName + "-nwfilter";
			if (!networkFilterExists(cnx, filter)) {
				log.trace("Creating Network Filter '" + filter
						+ "' (linked to Security Group '" + sSGName
						+ "') for Domain '" + sInstanceId + "' ...");
				cnx.networkFilterDefineXML(XPathExpander.expand(
						DOMAIN_NETWORK_FILTER_XML_SNIPPET, null, vars));
				log.debug("Network Filter '" + filter
						+ "' (linked to Security Group '" + sSGName
						+ "') created for Domain '" + sInstanceId + "'.");
			}

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

	public static NetworkDeviceDatas getInstanceNetworkDeviceDatas(Instance i,
			NetworkDevice netDev) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		return getInstanceNetworkDeviceDatas(i.getDomain(), netDev);
	}

	public static NetworkDeviceDatas getInstanceNetworkDeviceDatas(Domain d,
			NetworkDevice netDev) {
		if (netDev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDevice.class.getCanonicalName() + ".");
		}

		try {
			Doc ddoc = getDomainXMLDesc(d);
			String mac = ddoc.evaluateAsString("/domain/devices/interface"
					+ "[@type='network' and alias/@name='"
					+ netDev.getDeviceName().substring(3) + "']/mac/@address");
			NetworkDeviceDatas ndd = new NetworkDeviceDatas();
			ndd.setMacAddress(mac);
			ndd.setIP(getDomainIpAddress(mac));
			ndd.setFQDN(getDomainDnsName(mac));
			return ndd;
		} catch (XPathExpressionException Ex) {
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

	private static String generateUniqDomainName(Connect cnx)
			throws LibvirtException {
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

	protected static Doc getDomainXMLDesc(Domain domain) {
		if (domain == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			Doc doc = new Doc();
			doc.loadFromXML(domain.getXMLDesc(0));
			return doc;
		} catch (MelodyException | LibvirtException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String getDomainMacAddress(Domain domain) {
		try {
			Doc doc = getDomainXMLDesc(domain);
			return doc.evaluateAsString("//devices/interface[@type='network']"
					+ "[1]/mac/@address");
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

	public static boolean instanceExists(Connect cnx, String sInstanceId)
			throws LibvirtException {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		if (sInstanceId == null) {
			return false;
		}
		String[] names = cnx.listDefinedDomains();
		return Arrays.asList(names).contains(sInstanceId);
	}

	public static Instance getInstance(Connect cnx, String sInstanceId) {
		Domain d = getDomain(cnx, sInstanceId);
		if (d == null) {
			return null;
		}
		return new Instance(d);
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

	public static void createSecurityGroup(Connect cnx, String sSGName,
			String sSGDesc) {
		// Create a network filter for the network device
		try {
			if (networkFilterExists(cnx, sSGName)) {
				throw new RuntimeException(sSGName + ": network filter "
						+ "already exists.");
			}
			/*
			 * TODO : remove 'all accept NEW' when done
			 */
			String NETWORK_FILTER_XML_SNIPPET = "<filter name='" + sSGName
					+ "' chain='root'>"
					+ "<rule action='accept' direction='in' priority='500'>"
					+ "<all state='NEW'/>" + "</rule>" + "</filter>";
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

	public static void deleteSecurityGroups(Connect cnx, List<String> sgs) {
		for (String sg : sgs) {
			deleteSecurityGroup(cnx, sg);
		}
	}

	private static String DOMAIN_NETWORK_FILTER_XML_SNIPPET = "<filter name='§[vmName]§-§[eth]§-nwfilter' chain='root'>"
			+ "<filterref filter='§[sgName]§'/>"
			// TODO : this will drop packets sent to eth1 and reply sent by eth0
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

	public static Instance newInstance(Connect cnx, InstanceType type,
			String sImageId, String sSGName, KeyPairName keyPairName) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		if (sSGName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName());
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
			if (!networkFilterExists(cnx, sSGName)) {
				throw new RuntimeException(sSGName + ": No such network "
						+ "filter.");
			}

			Path ddt = getImageDomainDescriptor(sImageId);
			PropertiesSet ps = new PropertiesSet();
			Domain domain = null;
			String sInstanceId = null;
			// Defines domain
			synchronized (LOCK_UNIQ_DOMAIN) {
				// this block is sync because the sInstanceId must be consistent
				sInstanceId = generateUniqDomainName(cnx);
				try {
					ps.put(new Property("vmName", sInstanceId));
					ps.put(new Property("vmMacAddr", generateUniqMacAddress()));
					ps.put(new Property("vcpu", String.valueOf(getVCPU(type))));
					ps.put(new Property("ram", String.valueOf(getRAM(type))));
					ps.put(new Property("sgName", sSGName));
					ps.put(new Property("eth", "eth0"));
				} catch (IllegalPropertyException Ex) {
					throw new RuntimeException(Ex);
				}
				log.trace("Creating domain '" + sInstanceId
						+ "' based on the template " + sImageId
						+ " ... MacAddress is '"
						+ ps.getProperty("vmMacAddr").getValue() + "'.");
				try {
					domain = cnx.domainDefineXML(XPathExpander.expand(ddt,
							null, ps));
				} catch (XPathExpressionSyntaxException Ex) {
					throw new RuntimeException(ddt
							+ ": template contains invalid syntax");
				} catch (IOException e) {
					throw new RuntimeException(ddt
							+ ": IO error while expanding template");
				} catch (IllegalFileException Ex) {
					throw new RuntimeException(Ex);
				}
			}
			log.debug("Domain '" + sInstanceId + "' created.");

			// Create a network filter for the network device
			log.trace("Creating Network Filter '" + sInstanceId
					+ "-eth0-nwfilter' (linked to Security Group '" + sSGName
					+ "') for Domain '" + sInstanceId + "' ...");
			cnx.networkFilterDefineXML(XPathExpander.expand(
					DOMAIN_NETWORK_FILTER_XML_SNIPPET, null, ps));
			log.debug("Network Filter '" + sInstanceId
					+ "-eth0-nwfilter' (linked to Security Group '" + sSGName
					+ "') created for Domain '" + sInstanceId + "'.");

			// Create disk devices
			NodeList nl = null;
			nl = conf.evaluateAsNodeList("//images/image[@name='" + sImageId
					+ "']/disk");
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
			return new Instance(domain);
		} catch (LibvirtException | XPathExpressionSyntaxException
				| IllegalFileException | IOException | XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void deleteInstance(Connect cnx, String sInstanceId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		/*
		 * TODO : should be asynchronous.
		 * 
		 * Should shutdown (instead of destroy) the domain. Should handle a
		 * SHUTTING_DOWN and a TERMINATED state.
		 */
		NodeList nl = null;
		try {
			// get Domain
			Domain domain = getDomain(cnx, sInstanceId);
			DomainState state = domain.getInfo().state;
			Doc doc = getDomainXMLDesc(domain);
			// destroy domain
			if (state == DomainState.VIR_DOMAIN_RUNNING
					|| state == DomainState.VIR_DOMAIN_PAUSED) {
				log.trace("Destroying Domain '" + sInstanceId + "' ...");
				domain.destroy();
				log.debug("Domain '" + sInstanceId + "' destroyed.");
			}
			// release network devices and network Ffilters
			nl = doc.evaluateAsNodeList("/domain/devices/interface[@type='network']");
			for (int i = 0; i < nl.getLength(); i++) {
				// Destroy the network filter
				String filter = Doc.evaluateAsString("./filterref/@filter",
						nl.item(i));
				if (!networkFilterExists(cnx, filter)) {
					continue;
				}
				log.trace("Deleting Network Filter '" + filter
						+ "' for Domain '" + sInstanceId + "' ...");
				NetworkFilter nf = cnx.networkFilterLookupByName(filter);
				nf.undefine();
				log.debug("Network Filter '" + filter
						+ "' deleted for Domain '" + sInstanceId + "'.");
				// Release the @mac
				String mac = Doc.evaluateAsString("./mac/@address", nl.item(i));
				unregisterMacAddress(mac);
			}
			// destroy disks
			nl = doc.evaluateAsNodeList("/domain/devices/disk[@device='disk']"
					+ "/source/@file");
			for (int i = 0; i < nl.getLength(); i++) {
				String sDiskDeviceName = "/dev/"
						+ Doc.evaluateAsString("../target/@dev",
								((Attr) nl.item(i)).getOwnerElement());
				StorageVol sv = cnx.storageVolLookupByPath(nl.item(i)
						.getNodeValue());
				log.trace("Deleting Disk Device '" + sDiskDeviceName
						+ "' for Domain '" + sInstanceId
						+ "' ... LibVirt Volume path is '" + sv.getPath()
						+ "'.");
				sv.delete(0);
				log.debug("Disk Device '" + sDiskDeviceName
						+ "' deleted for Domain '" + sInstanceId + "'.");
			}
			// undefine domain
			domain.undefine();
		} catch (LibvirtException | XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

}