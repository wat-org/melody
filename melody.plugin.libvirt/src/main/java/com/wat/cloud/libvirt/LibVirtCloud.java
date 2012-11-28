package com.wat.cloud.libvirt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.common.utils.exception.IllegalPropertyException;
import com.wat.melody.common.utils.exception.MelodyException;
import com.wat.melody.plugin.libvirt.common.InstanceState;
import com.wat.melody.plugin.libvirt.common.InstanceType;
import com.wat.melody.plugin.libvirt.common.exception.IllegalInstanceStateException;
import com.wat.melody.xpathextensions.XPathExpander;
import com.wat.melody.xpathextensions.common.exception.XPathExpressionSyntaxException;

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
					Tools.validateFileExists(descriptor.getNodeValue());
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
			}
		}
	}

	public static final String SIZE_PATTERN = "([0-9]+)[\\s]?([tTgGmM])";
	public static Pattern p = Pattern.compile("^" + SIZE_PATTERN + "$");

	public static int getRAM(InstanceType type) {
		String sRam = null;
		try {
			sRam = sizeconf.evaluateAsString("/sizings/sizing[@name='" + type
					+ "']/@ram");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		Matcher matcher = p.matcher(sRam);
		matcher.matches();
		int iRam = Integer.parseInt(matcher.group(1));
		switch (matcher.group(2).charAt(0)) {
		case 't':
		case 'T':
			iRam *= 1024 * 1024 * 1024;
			break;
		case 'g':
		case 'G':
			iRam *= 1024 * 1024;
			break;
		case 'm':
		case 'M':
			iRam *= 1024;
			break;
		}
		return iRam;
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

	protected static InstanceType getDomainType(Domain d) {
		/*
		 * TODO : deduce the instance type from the VCPU and RAM defined in the
		 * instance
		 */
		return InstanceType.T1Micro;
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
	 * @return the RAM quantity in KiloOctet
	 */
	public static int getDomainRAM(Domain domain) {
		try {
			Doc doc = getDomainXMLDesc(domain);
			int ram = Integer.parseInt(doc
					.evaluateAsString("/domain/vcpu/text()"));
			String unit = doc.evaluateAsString("/domain/vcpu/@unit");
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
		String sNewName = null;
		while (true) {
			sNewName = "i-" + UUID.randomUUID().toString().substring(0, 8);
			if (!instanceExists(cnx, sNewName)) {
				return sNewName;
			}
		}
	}

	private static String generateUniqDomainUUID() {
		return UUID.randomUUID().toString();
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
		log.trace("Allocating Mac Address '" + sFirstFreeMacAddr + "'.");
		Doc.createAttribute("allocated", "true", nlFreeMacAddrPool.item(0));
		netconf.store();
		log.debug("Mac Address '" + sFirstFreeMacAddr + "' allocated.");
		return sFirstFreeMacAddr;
	}

	private static synchronized void unregisterMacAddress(String sMacAddr) {
		log.trace("Releasing Mac Address '" + sMacAddr + "'.");
		Node nMacAddr = null;
		try {
			nMacAddr = netconf.evaluateAsNode("/network/ip/dhcp" + "/host"
					+ "[ @mac='" + sMacAddr + "' and exists(@allocated) ]");
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

	private static Doc getDomainXMLDesc(Domain domain) {
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
		try {
			return netconf.evaluateAsString("/network/ip/dhcp"
					+ "/host[ @mac='" + sMacAddr + "' ]/@ip");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.");
		}
	}

	protected static String getDomainDnsName(String sMacAddr) {
		try {
			return netconf.evaluateAsString("/network/ip/dhcp"
					+ "/host[ @mac='" + sMacAddr + "' ]/@name");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.");
		}
	}

	public static boolean imageIdExists(String sImageId) {
		Node n = null;
		try {
			n = conf.evaluateAsNode("//images/image[@name='" + sImageId + "']");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Hard coded xpath expression is not "
					+ "valid. Check the source code.", Ex);
		}
		return n != null;
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
		String[] names = cnx.listDefinedDomains();
		return Arrays.asList(names).contains(sInstanceId);
	}

	public static Instance getInstance(Connect cnx, String sInstanceId) {
		return new Instance(getDomain(cnx, sInstanceId));
	}

	public static InstanceState getInstanceState(Connect cnx,
			String sAwsInstanceId) {
		Domain i = getDomain(cnx, sAwsInstanceId);
		if (i == null) {
			return null;
		}
		DomainState state = null;
		try {
			state = i.getInfo().state;
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
		try {
			return InstanceState.parseDomainState(state);
		} catch (IllegalInstanceStateException Ex) {
			throw new RuntimeException("Unexpected error while creating an "
					+ "InstanceState Enum based on the value '" + state + "'. "
					+ "Because this value was given by the AWS API, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public static boolean instanceLives(Connect cnx, String sAwsInstanceId) {
		InstanceState cs = getInstanceState(cnx, sAwsInstanceId);
		if (cs == null) {
			return false;
		}
		return true;
	}

	private static String LOCK_UNIQ_DOMAIN = "";
	private static String LOCK_CLONE_DISK = "";

	public static Instance newInstance(Connect cnx, InstanceType type,
			String sImageId, String sKeyName) {
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

			Path ddt = getImageDomainDescriptor(sImageId);
			PropertiesSet ps = new PropertiesSet();
			Domain domain = null;
			synchronized (LOCK_UNIQ_DOMAIN) {
				// so that the UniqDomainName is consistent
				try {
					ps.put(new Property("vmName", generateUniqDomainName(cnx)));
					ps.put(new Property("uuid", generateUniqDomainUUID()));
					ps.put(new Property("vmMacAddr", generateUniqMacAddress()));
					ps.put(new Property("vcpu", String.valueOf(type.getVCPU())));
					ps.put(new Property("ram", String.valueOf(type.getRAM())));
				} catch (IllegalPropertyException Ex) {
					throw new RuntimeException(Ex);
				}
				log.trace("Creating domain '"
						+ ps.getProperty("vmName").getValue()
						+ "' based on the template " + sImageId
						+ ". MacAddress is '"
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
			log.debug("Domain '" + ps.getProperty("vmName").getValue()
					+ "' created.");

			NodeList nl = null;
			try {
				nl = conf.evaluateAsNodeList("//images/image[@name='"
						+ sImageId + "']/disk");
			} catch (XPathExpressionException Ex) {
				throw new RuntimeException(Ex);
			}
			StoragePool sp = cnx.storagePoolLookupByName("default");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				Node descriptor = n.getAttributes().getNamedItem("descriptor");
				Node source = n.getAttributes().getNamedItem("source");
				String sDescriptorPath = descriptor.getNodeValue();
				String sSourceVolumePath = source.getNodeValue();
				log.trace("Creating domain '"
						+ ps.getProperty("vmName").getValue() + "' volume "
						+ (i + 1) + " from Volume '" + sSourceVolumePath + "'.");
				StorageVol sourceVolume = cnx
						.storageVolLookupByPath(sSourceVolumePath);
				String sDescriptor = null;
				try {
					sDescriptor = XPathExpander.expand(
							Paths.get(sDescriptorPath), null, ps);
				} catch (ExpressionSyntaxException e) {
					throw new RuntimeException(sDescriptorPath
							+ ": template contains invalid syntax");
				} catch (IOException e) {
					throw new RuntimeException(sDescriptorPath
							+ ": IO error while expanding template");
				} catch (IllegalFileException Ex) {
					throw new RuntimeException(Ex);
				}
				StorageVol dest = null;
				synchronized (LOCK_CLONE_DISK) {
					// we can't clone a volume which is already being cloned
					// and we can't use nio.Files.copy which is 4 times slower
					dest = sp.storageVolCreateXMLFrom(sDescriptor,
							sourceVolume, 0);
				}
				log.debug("Domain '" + ps.getProperty("vmName").getValue()
						+ "' volume " + (i + 1) + " created. Volume path is '"
						+ dest.getPath() + "'.");
			}

			log.trace("Starting domain '" + ps.getProperty("vmName").getValue()
					+ "'.");
			domain.create();
			log.debug("Domain '" + ps.getProperty("vmName").getValue()
					+ "' started.");
			return new Instance(domain);
		} catch (LibvirtException Ex) {
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
		try {
			// get Domain
			Domain domain = getDomain(cnx, sInstanceId);
			DomainState state = domain.getInfo().state;
			Doc doc = getDomainXMLDesc(domain);
			// destroy domain
			if (state == DomainState.VIR_DOMAIN_RUNNING
					|| state == DomainState.VIR_DOMAIN_PAUSED) {
				log.trace("Destroying domain '" + sInstanceId + "'.");
				domain.destroy();
				log.debug("Domain '" + sInstanceId + "' destroyed.");
			}
			// destroy disks
			NodeList nl = null;
			try {
				nl = doc.evaluateAsNodeList("/domain/devices/disk[@device='disk']"
						+ "/source/@file");
			} catch (XPathExpressionException Ex) {
				throw new RuntimeException(Ex);
			}
			for (int i = 0; i < nl.getLength(); i++) {
				StorageVol sv = cnx.storageVolLookupByPath(nl.item(i)
						.getNodeValue());
				log.trace("Deleting domain '" + sInstanceId + "' volume "
						+ (i + 1) + ". Volume path iss '" + sv.getPath() + "'.");
				sv.delete(0);
				log.debug("Domain '" + sInstanceId + "' volume " + (i + 1)
						+ " deleted.");
			}
			// undefine domain
			domain.undefine();
			// free macAddr
			String sMac = null;
			try {
				sMac = doc.evaluateAsString("//devices/interface"
						+ "[@type='network'][1]/mac/@address");
			} catch (XPathExpressionException Ex) {
				throw new RuntimeException(Ex);
			}
			unregisterMacAddress(sMac);
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

}