package com.wat.cloud.libvirt;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.exception.XPathExpressionSyntaxException;

/**
 * <p>
 * Quick and dirty class which provide libvirt network management features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudNetwork {

	private static Log log = LogFactory.getLog(LibVirtCloudNetwork.class);

	public static final String LIBVIRT_CLOUD_NET_CONF = "/Cloud/libvirt/conf-net.xml";
	protected static Doc netconf = loadLibVirtCloudNetworkConfiguration();

	private static Doc loadLibVirtCloudNetworkConfiguration() {
		Doc doc = new Doc();
		try {
			doc.load(LIBVIRT_CLOUD_NET_CONF);
		} catch (MelodyException | IOException Ex) {
			throw new RuntimeException(
					"Failed to load LibVirtCloud Network Configuration File '"
							+ LIBVIRT_CLOUD_NET_CONF + "'.", Ex);
		}
		return doc;
	}

	public static NetworkDeviceNameList getNetworkDevices(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		try {
			NetworkDeviceNameList ndl = new NetworkDeviceNameList();
			Doc doc = LibVirtCloud.getDomainXMLDesc(d);
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

	private static final String DETACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
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
			PropertySet vars = new PropertySet();
			vars.put(new Property("vmMacAddr", mac));

			// Detach the network device
			log.trace("Detaching Network Device '" + netDevName
					+ "' on Domain '" + sInstanceId + "' ...");
			int flag = LibVirtCloud.getDomainState(d) == InstanceState.RUNNING ? 3
					: 2;
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

	private static final String ATTACH_NETWORK_DEVICE_XML_SNIPPET = "<interface type='network'>"
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
			PropertySet vars = new PropertySet();
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
			int flag = LibVirtCloud.getDomainState(d) == InstanceState.RUNNING ? 3
					: 2;
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

	public static String getDomainMacAddress(Domain d, NetworkDeviceName netdev) {
		if (netdev == null) {
			netdev = eth0;
		}
		try {
			Doc doc = LibVirtCloud.getDomainXMLDesc(d);
			return doc.evaluateAsString("/domain/devices/interface"
					+ "[@type='network' and filterref/@filter='"
					+ getNetworkFilter(d, netdev) + "']/mac/@address");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static String getDomainIpAddress(String sMacAddr) {
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

	public static String getDomainDnsName(String sMacAddr) {
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

	protected static synchronized String generateUniqMacAddress() {
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
		((Element) nlFreeMacAddrPool.item(0)).setAttribute("allocated", "true");
		netconf.store();
		log.debug("Mac Address '" + sFirstFreeMacAddr + "' allocated.");
		return sFirstFreeMacAddr;
	}

	protected static synchronized void unregisterMacAddress(String sMacAddr) {
		if (sMacAddr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		log.trace("Releasing Mac Address '" + sMacAddr + "' ...");
		Element nMacAddr = null;
		try {
			nMacAddr = (Element) netconf.evaluateAsNode("/network/ip/dhcp/host"
					+ "[ upper-case(@mac)=upper-case('" + sMacAddr
					+ "') and exists(@allocated) ]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (nMacAddr == null) {
			return;
		}
		nMacAddr.removeAttribute("allocated");
		netconf.store();
		log.debug("Mac Address '" + sMacAddr + "' released.");
	}

	private static boolean networkFilterExists(Connect cnx, String sSGName)
			throws LibvirtException {
		if (sSGName == null) {
			return false;
		}
		String[] names = cnx.listNetworkFilters();
		return Arrays.asList(names).contains(sSGName);
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
	private static String getNetworkFilter(Domain d, NetworkDeviceName netdev) {
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

	protected static void createNetworkFilter(Domain d, PropertySet ps) {
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

	protected static void deleteNetworkFilter(Domain d, NetworkDeviceName netdev) {
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

	protected static String getSecurityGroup(Domain d, NetworkDeviceName netdev) {
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

	protected static void createSecurityGroup(Connect cnx, String sSGName,
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

	protected static void deleteSecurityGroup(Connect cnx, String sSGName) {
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

	protected static String getSecurityGroupDescription() {
		return "Melody security group";
	}

	protected static String newSecurityGroupName() {
		// This formula should produce a unique name
		return "MelodySg" + "_" + System.currentTimeMillis() + "_"
				+ UUID.randomUUID().toString().substring(0, 8);
	}

	protected static NetworkDeviceName eth0 = createNetworkDeviceName("eth0");

	private static NetworkDeviceName createNetworkDeviceName(String n) {
		try {
			return NetworkDeviceName.parseString(n);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new RuntimeException(Ex);
		}
	}

}