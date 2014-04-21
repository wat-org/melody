package com.wat.cloud.libvirt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.Error.ErrorNumber;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.exception.ProtectedAreaNotFoundException;
import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * <p>
 * Quick and dirty class which provides access to libvirt features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloud {

	private static Logger log = LoggerFactory.getLogger(LibVirtCloud.class);

	public static final String LIBVIRT_CLOUD_IMG_CONF = "/Cloud/libvirt/conf.xml";
	protected static Doc conf = loadLibVirtCloudConfiguration();

	public static final String LIBVIRT_CLOUD_SIZE_CONF = "/Cloud/libvirt/conf-sizing.xml";
	protected static Doc sizeconf = loadLibVirtCloudSizingConfiguration();

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

	private static Doc loadLibVirtCloudSizingConfiguration() {
		Doc doc = new Doc();
		try {
			doc.load(LIBVIRT_CLOUD_SIZE_CONF);
		} catch (MelodyException | IOException Ex) {
			throw new RuntimeException(
					"Failed to load LibVirtCloud Network Configuration File '"
							+ LIBVIRT_CLOUD_SIZE_CONF + "'.", Ex);
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
			Element n = (Element) nl.item(j);
			Node name = n.getAttributeNode("name");
			if (name == null) {
				throw new RuntimeException("Image is not valid. "
						+ "'name' XML attribute cannot be found.");
			}
			String sImageId = name.getNodeValue();
			Node descriptor = n.getAttributeNode("descriptor");
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
			throw new RuntimeException(sizeconf.getSourceFile()
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

	public static Domain getDomain(Connect cnx, String sInstanceId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName());
		}
		if (sInstanceId == null) {
			return null;
		}
		/*
		 * When domainLookupByName() cannot found the given domain, it writes
		 * "libvir: QEMU error : Domain not found: no domain with matching name '<sInstanceId>'"
		 * in System.err. This test prevent this error to be writen.
		 */
		if (!instanceExists(cnx, sInstanceId)) {
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
			// iterate through the active domains
			for (int activeDomainId : cnx.listDomains()) {
				Domain d = cnx.domainLookupByID(activeDomainId);
				if (d.getName().equals(sInstanceId)) {
					return true;
				}
			}
			// search in the inactive domains
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
			log.debug("Waiting for Domain for Instance '" + sInstanceId
					+ "' to become '" + state + "'. Currently '" + is + "'.");
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

	private static String LOCK_UNIQ_DOMAIN = "domain_lock";
	private static String LOCK_CLONE_DISK = "disk_lock";

	/*
	 * TODO : The caller must deal with ProtectedAreaNotFoundException.
	 * 
	 * What's the behavior of AWS Plug-In in this situation ?
	 */
	public static String newInstance(Connect cnx, InstanceType type,
			String sImageId, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds)
			throws ProtectedAreaNotFoundException {
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

			NetworkDeviceName eth0Name = LibVirtCloudNetwork.eth0;

			Path ddt = getImageDomainDescriptor(sImageId);
			PropertySet ps = new PropertySet();
			Domain domain = null;
			String sInstanceId = null;
			String sMacAddr = null;
			// Defines domain
			synchronized (LOCK_UNIQ_DOMAIN) {// domain's name must be consistent
				sInstanceId = generateUniqDomainName(cnx);
				log.trace("Creating domain '" + sInstanceId + "' ...");
				sMacAddr = LibVirtCloudNetwork.generateUniqMacAddress();
				// Create the master network filter
				try {
					LibVirtCloudNetwork.createMasterNetworkFilter(cnx,
							sInstanceId, eth0Name, sMacAddr, protectedAreaIds);
				} catch (ProtectedAreaNotFoundException Ex) {
					// release @mac when ProtectedAreaNotFoundException
					log.error("Fail to create Master Network Filter on "
							+ "Network Device '" + eth0Name + "' of Domain '"
							+ sInstanceId
							+ "'. Rolling-back Mac Address allocation ...");
					LibVirtCloudNetwork.unregisterMacAddress(sMacAddr);
					log.debug("Mac Address " + sMacAddr
							+ " allocation rolled-back on Network Device '"
							+ eth0Name + "' of Domain '" + sInstanceId + "'.");
					throw Ex;
				}

				// Create the Domain
				ps.put(new Property("vmName", sInstanceId));
				ps.put(new Property("vmMacAddr", sMacAddr));
				ps.put(new Property("vcpu", String.valueOf(getVCPU(type))));
				ps.put(new Property("ram", String.valueOf(getRAM(type))));
				ps.put(new Property("eth", eth0Name.getValue()));
				domain = cnx.domainDefineXML(XPathExpander
						.expand(ddt, null, ps));
			}
			// Associate the keypair
			LibVirtCloudKeyPair.associateKeyPairToInstance(domain, keyPairName);

			// Create disk devices
			NodeList nl = null;
			nl = conf.evaluateAsNodeList("//images/image[@name='" + sImageId
					+ "']/disk");
			StoragePool sp = cnx.storagePoolLookupByName("default");
			for (int i = 0; i < nl.getLength(); i++) {
				Element n = (Element) nl.item(i);
				String sDescriptorPath = n.getAttribute("descriptor");
				String sSourceVolumePath = n.getAttribute("source");
				String sDiskDeviceName = n.getAttribute("device");
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
					// and we can't use nio.Files.copy which is 4 times
					// slower
					sv = sp.storageVolCreateXMLFrom(sDescriptor, sourceVolume,
							0);
				}
				log.debug("Disk Device '" + sDiskDeviceName
						+ "' created for Domain '" + sInstanceId
						+ "'. LibVirt Volume path is '" + sv.getPath() + "'.");
			}

			// Start domain
			log.trace("Starting Domain '" + sInstanceId + "' ...");
			domain.create();
			log.debug("Domain '" + sInstanceId + "' started.");
			log.debug("Domain '" + sInstanceId + "' created (template:"
					+ sImageId + ", mac-address:" + sMacAddr + ", key-pair:"
					+ keyPairName + ").");
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
			String sInstanceId = d.getName();
			log.trace("destroying Domain '" + sInstanceId + "' ...");
			DomainState state = d.getInfo().state;
			// Destroy domain
			if (state == DomainState.VIR_DOMAIN_RUNNING
					|| state == DomainState.VIR_DOMAIN_PAUSED) {
				log.trace("Stopping Domain '" + sInstanceId + "' ...");
				d.destroy();
				log.debug("Domain '" + sInstanceId + "' stopped.");
			}
			// De associate keyPair
			LibVirtCloudKeyPair.deassociateKeyPairToInstance(d);

			// Release network devices
			NetworkDeviceList netdevs = LibVirtCloudNetwork
					.getNetworkDevices(d);
			for (NetworkDevice netdev : netdevs) {
				NetworkDeviceName devname = netdev.getNetworkDeviceName();
				// Destroy the master network filter
				LibVirtCloudNetwork.deleteMasterNetworkFilter(d, devname);
				// Release the @mac
				String mac = LibVirtCloudNetwork
						.getDomainMacAddress(d, devname);
				LibVirtCloudNetwork.unregisterMacAddress(mac);
			}
			// Destroy disk devices
			DiskDeviceList diskdevs = LibVirtCloudDisk.getDiskDevices(d);
			for (DiskDevice disk : diskdevs) {
				// Destroy disk device
				LibVirtCloudDisk.deleteDiskDevice(d, disk);
			}
			// Undefine domain
			d.undefine();
			log.debug("Domain '" + sInstanceId + "' destroyed.");
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
			/*
			 * Sometimes, Windows VM doesn't stop on the first call to shutdown.
			 * Need to call shutdown twice.
			 */
			Thread.sleep(1000);
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
			 * Memory hotplug/unplug is a fake (it acts on balloning!), and cpu
			 * unplug is not supported. For these reason, we only deal with
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
			 * we cannot use Domain.setMemory and Domain.setVcpus, because it
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