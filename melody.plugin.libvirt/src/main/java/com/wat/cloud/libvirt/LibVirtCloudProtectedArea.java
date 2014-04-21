package com.wat.cloud.libvirt;

import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.exception.ProtectedAreaNotFoundException;
import com.wat.cloud.libvirt.exception.ProtectedAreaStillInUseException;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.IcmpCode;
import com.wat.melody.common.firewall.IcmpType;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.Protocol;
import com.wat.melody.common.firewall.SimpleAbstractTcpUdpFireWallwRule;
import com.wat.melody.common.firewall.SimpleFireWallRule;
import com.wat.melody.common.firewall.SimpleIcmpFireWallRule;
import com.wat.melody.common.firewall.SimpleTcpFireWallRule;
import com.wat.melody.common.firewall.SimpleUdpFireWallRule;
import com.wat.melody.common.firewall.exception.IllegalAccessException;
import com.wat.melody.common.firewall.exception.IllegalDirectionException;
import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.firewall.exception.IllegalProtocolException;
import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * Quick and dirty class which provides libvirt protected area management
 * features.
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudProtectedArea {

	private static Logger log = LoggerFactory
			.getLogger(LibVirtCloudProtectedArea.class);

	/**
	 * caller should synchronize this call with the store operation
	 */
	private static ProtectedAreaId generateUniqProtectedAreaId(Connect cnx) {
		String sgId = null;
		while (true) {
			sgId = "pa-" + UUID.randomUUID().toString().substring(0, 8);
			if (!protectedAreaExists(cnx, sgId)) {
				break;
			}
		}
		try {
			return ProtectedAreaId.parseString(sgId);
		} catch (IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException("Fail to convert '" + sgId + "' into '"
					+ ProtectedAreaId.class.getCanonicalName() + "'. "
					+ "If this error happened, you should modify the "
					+ "conversion rule.", Ex);
		}
	}

	public static boolean protectedAreaExists(Connect cnx, String spaId) {
		if (spaId == null) {
			return false;
		}
		try {
			Element areaNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + spaId + "']");
			return areaNode != null;
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static boolean protectedAreaExists(Connect cnx, ProtectedAreaId paId) {
		if (paId == null) {
			return false;
		}
		return protectedAreaExists(cnx, paId.getValue());
	}

	private static String LOCK_UNIQ_PAID = "protected_area_lock";

	public static ProtectedAreaId createProtectedArea(Connect cnx,
			ProtectedAreaName name, String description) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaName.class.getCanonicalName() + ".");
		}
		if (description == null) {
			description = "default-description";
		}
		try {
			// register the new protected area in the libvirt conf
			Element areasNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas");
			Document doc = areasNode.getOwnerDocument();

			ProtectedAreaId paId = null;
			synchronized (LOCK_UNIQ_PAID) {// pa id must be unique
				paId = generateUniqProtectedAreaId(cnx);
				log.trace("Creating Protected Area '" + paId
						+ "' in LibVirt Cloud ...");
				Element areaNode = doc.createElement("protected-area");
				areaNode.setAttribute("id", paId.getValue());
				areaNode.setAttribute("name", name.getValue());
				areaNode.setAttribute("description", description);
				areaNode.appendChild(doc.createElement("domains"));
				areaNode.appendChild(doc.createElement("rules"));

				areasNode.appendChild(areaNode);
				LibVirtCloud.conf.store();
			}
			// create the corresponding network filter
			LibVirtCloudNetwork.createNetworkFilter(cnx, paId.getValue());

			log.debug("Protected Area '" + paId + "' created in LibVirt Cloud.");
			return paId;
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void destroyProtectedArea(Connect cnx, ProtectedAreaId paId)
			throws ProtectedAreaStillInUseException {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		try {
			log.trace("Destroying Protected Area '" + paId
					+ "' in LibVirt Cloud ...");
			Element areaNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']");
			if (areaNode == null) {
				log.debug("No need to delete Protected Area '" + paId
						+ "' cause it doesn't exists.");
			}
			// verify that no domains are using the protected area to remove
			NodeList domains = XPathExpander.evaluateAsNodeList("./domains/*",
					areaNode);
			if (domains != null && domains.getLength() > 0) {
				throw new ProtectedAreaStillInUseException(paId);
			}

			// verify that no other PA are using the PA to remove
			NodeList usingNode = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area/rules/rule[@from-ip='" + paId
							+ "' or @to-ip='" + paId + "']");
			if (usingNode != null && usingNode.getLength() > 0) {
				throw new ProtectedAreaStillInUseException(paId);
			}

			// unregister the protected area in the libvirt conf
			areaNode.getParentNode().removeChild(areaNode);
			LibVirtCloud.conf.store();
			// destroy the corresponding network filter
			LibVirtCloudNetwork.deleteNetworkFilter(cnx, paId.getValue());
			log.debug("Protected Area '" + paId
					+ "' destroyed in LibVirt Cloud.");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static ProtectedAreaIds getProtectedAreas(String sInstanceId) {
		/*
		 * get all protected area associated with the given domain, but reject
		 * all self protected area
		 */
		try {
			ProtectedAreaIds protectedAreaIds = new ProtectedAreaIds();
			NodeList paNodes = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area[ domains/domain[@id='"
							+ sInstanceId
							+ "'] and "
							+ "not(matches(@name,'^melody-self-protected-area:'))]");
			if (paNodes == null || paNodes.getLength() == 0) {
				return protectedAreaIds;
			}
			for (int i = 0; i < paNodes.getLength(); i++) {
				Element pa = (Element) paNodes.item(i);
				String spaid = pa.getAttribute("id");
				ProtectedAreaId paid = ProtectedAreaId.parseString(spaid);
				protectedAreaIds.add(paid);
			}
			return protectedAreaIds;
		} catch (XPathExpressionException | IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void associateProtectedAreas(Connect cnx,
			String sInstanceId, NetworkDeviceName devname, String sMacAddr,
			ProtectedAreaIds paIds) throws ProtectedAreaNotFoundException {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (sInstanceId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		if (sMacAddr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (paIds == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaIds.class.getCanonicalName() + ".");
		}
		log.trace("Associating Protected Areas " + paIds
				+ " on Network Device '" + devname + "' of Domain '"
				+ sInstanceId + "' ...");
		try {
			for (ProtectedAreaId paId : paIds) {
				// register domain in the protected area
				Element domainsNode = (Element) LibVirtCloud.conf
						.evaluateAsNode("/libvirtcloud/protected-areas/"
								+ "protected-area[@id='" + paId + "']/domains");
				if (domainsNode == null) {
					ProtectedAreaNotFoundException Ex = null;
					Ex = new ProtectedAreaNotFoundException(paId);
					log.error(new MelodyException("Fail to associate "
							+ "Protected Area " + paId + " on Network "
							+ "Device '" + devname + "' of Domain '"
							+ sInstanceId + "'. Rolling-back Protected "
							+ "Areas association...", Ex).getFullStackTrace());
					deassociateProtectedAreas(cnx, sInstanceId, devname);
					log.debug("Protected Areas " + paIds + " association "
							+ "rolled-back on Network Device '" + devname
							+ "' of Domain '" + sInstanceId + "'.");
					throw Ex;
				}
				Document doc = domainsNode.getOwnerDocument();
				Element domainNode = doc.createElement("domain");
				domainNode.setAttribute("id", sInstanceId);
				domainNode.setAttribute("netdevname", devname.getValue());
				domainNode.setAttribute("mac-addr", sMacAddr);
				domainsNode.appendChild(domainNode);
				// update the protected area where the domain is located
				updateAllProtectedAreasRelatedTo(cnx, paId);
			}
			LibVirtCloud.conf.store();
			log.debug("Protected Areas " + paIds
					+ " associated on Network Device '" + devname
					+ "' of Domain '" + sInstanceId + "'.");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void deassociateProtectedAreas(Connect cnx,
			String sInstanceId, NetworkDeviceName devname) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (sInstanceId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			log.trace("De-associating Protected Areas on Network Device '"
					+ devname + "' of Domain '" + sInstanceId + "' ...");
			NodeList domainNodes = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area/domains/domain[@id='"
							+ sInstanceId + "' and @netdevname='" + devname
							+ "']");
			if (domainNodes == null || domainNodes.getLength() == 0) {
				// not associated => nothing to do
				return;
			}
			// synchronize the loop to avoid concurrent access to the doc
			for (int i = 0; i < domainNodes.getLength(); i++) {
				Element domainNode = (Element) domainNodes.item(i);
				// get the protected area id
				String sPaId = ((Element) domainNode.getParentNode()
						.getParentNode()).getAttribute("id");
				ProtectedAreaId paId = ProtectedAreaId.parseString(sPaId);
				// unregister domain in the protected area
				domainNode.getParentNode().removeChild(domainNode);
				// update the protected area where the domain is located
				updateAllProtectedAreasRelatedTo(cnx, paId);
			}
			LibVirtCloud.conf.store();
			log.trace("Protected Areas de-associated on Network Device '"
					+ devname + "' of Domain '" + sInstanceId + "'.");
		} catch (XPathExpressionException | IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static FireWallRules getFireWallRules(Domain d,
			NetworkDeviceName devname) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		ProtectedAreaId paId = LibVirtCloudNetwork.getSelftProtectedAreaId(d,
				devname);
		return getFireWallRules(d.getConnect(), paId);
	}

	public static FireWallRules getFireWallRules(Connect cnx,
			ProtectedAreaId paId) {
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		if (!protectedAreaExists(cnx, paId)) {
			throw new IllegalArgumentException(paId + ": Not accepted. "
					+ "This Protected Area doesn't exists!");
		}
		try {
			FireWallRules rules = new FireWallRules();
			NodeList paRuleNodes = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']/rules/*");
			Element paRuleNode = null;
			for (int i = 0; i < paRuleNodes.getLength(); i++) {
				paRuleNode = (Element) paRuleNodes.item(i);
				Protocol proto = Protocol.parseString(paRuleNode.getNodeName());
				SimpleFireWallRule rule = null;
				switch (proto) {
				case TCP:
					rule = createTcpRuleFromElement(paRuleNode);
					break;
				case UDP:
					rule = createUdpRuleFromElement(paRuleNode);
					break;
				case ICMP:
					rule = createIcmpRuleFromElement(paRuleNode);
					break;
				}
				if (rule == null) {
					continue;
				}
				rules.add(rule);
			}
			return rules;
		} catch (XPathExpressionException | IllegalProtocolException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	// convert to ProtectedAreaId or IpRange
	private static Address convertToAddr(String sAddr) {
		try {
			return IpRange.parseString(sAddr);
		} catch (IllegalIpRangeException Ex) {
			// not an IpRange
		}
		// try to convert to ProtectedAreaName
		try {
			return ProtectedAreaId.parseString(sAddr);
		} catch (IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException(sAddr
					+ ": neither an IpRange, nor a ProtectedAreaId.");
		}
	}

	private static SimpleFireWallRule createTcpRuleFromElement(Element n) {
		try {
			String sAddr = n.getAttribute("from-ip");
			Address fromAddr = convertToAddr(sAddr);

			String sPort = n.getAttribute("from-port");
			PortRange fromPortRange = PortRange.parseString(sPort);

			sAddr = n.getAttribute("to-ip");
			Address toAddr = convertToAddr(sAddr);

			sPort = n.getAttribute("to-port");
			PortRange toPortRange = PortRange.parseString(sPort);

			String sDir = n.getAttribute("direction");
			Direction dir = Direction.parseString(sDir);

			String sAccess = n.getAttribute("access");
			Access access = Access.parseString(sAccess);
			return new SimpleTcpFireWallRule(fromAddr, fromPortRange, toAddr,
					toPortRange, dir, access);
		} catch (IllegalPortRangeException | IllegalDirectionException
				| IllegalAccessException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static SimpleFireWallRule createUdpRuleFromElement(Element n) {
		try {
			String sAddr = n.getAttribute("from-ip");
			Address fromAddr = convertToAddr(sAddr);

			String sPort = n.getAttribute("from-port");
			PortRange fromPortRange = PortRange.parseString(sPort);

			sAddr = n.getAttribute("to-ip");
			Address toAddr = convertToAddr(sAddr);

			sPort = n.getAttribute("to-port");
			PortRange toPortRange = PortRange.parseString(sPort);

			String sDir = n.getAttribute("direction");
			Direction dir = Direction.parseString(sDir);

			String sAccess = n.getAttribute("access");
			Access access = Access.parseString(sAccess);
			return new SimpleUdpFireWallRule(fromAddr, fromPortRange, toAddr,
					toPortRange, dir, access);
		} catch (IllegalPortRangeException | IllegalDirectionException
				| IllegalAccessException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static SimpleFireWallRule createIcmpRuleFromElement(Element n) {
		try {
			String sAddr = n.getAttribute("from-ip");
			Address fromAddr = convertToAddr(sAddr);

			sAddr = n.getAttribute("to-ip");
			Address toAddr = convertToAddr(sAddr);

			String sCode = n.getAttribute("code");
			IcmpCode code = IcmpCode.parseString(sCode);

			String sType = n.getAttribute("type");
			IcmpType type = IcmpType.parseString(sType);

			String sDir = n.getAttribute("direction");
			Direction dir = Direction.parseString(sDir);

			String sAccess = n.getAttribute("access");
			Access access = Access.parseString(sAccess);
			return new SimpleIcmpFireWallRule(fromAddr, toAddr, type, code,
					dir, access);
		} catch (IllegalDirectionException | IllegalAccessException
				| IllegalIcmpCodeException | IllegalIcmpTypeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void authorizeFireWallRules(Domain d,
			NetworkDeviceName devname, FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			String sInstanceId = d.getName();
			Connect cnx = d.getConnect();
			ProtectedAreaId paId = LibVirtCloudNetwork.getSelftProtectedAreaId(
					d, devname);
			Element paRulesNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']/rules");
			for (SimpleFireWallRule rule : rules) {
				Element paRuleNode = paRulesNode.getOwnerDocument()
						.createElement(rule.getProtocol().getValue());
				switch (rule.getProtocol()) {
				case TCP:
					createTcpUdpRuleElement(paRuleNode,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					createTcpUdpRuleElement(paRuleNode,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					createIcmpRuleElement(paRuleNode,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				paRulesNode.appendChild(paRuleNode);
				log.debug("Domain '" + sInstanceId + "' grants '" + devname
						+ "' the FireWall rule " + rule + ".");
			}
			LibVirtCloud.conf.store();
			// update related network filter's content
			LibVirtCloudFireWall.authorizeFireWallRules(cnx, paId,
					expand(rules));
		} catch (XPathExpressionException | LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void authorizeFireWallRules(Connect cnx,
			ProtectedAreaId paId, FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		if (!protectedAreaExists(cnx, paId)) {
			throw new IllegalArgumentException(paId + ": Not accepted. "
					+ "This Protected Area doesn't exists!");
		}
		try {
			Element paRulesNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']/rules");
			for (SimpleFireWallRule rule : rules) {
				Element paRuleNode = paRulesNode.getOwnerDocument()
						.createElement(rule.getProtocol().getValue());
				switch (rule.getProtocol()) {
				case TCP:
					createTcpUdpRuleElement(paRuleNode,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					createTcpUdpRuleElement(paRuleNode,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					createIcmpRuleElement(paRuleNode,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				paRulesNode.appendChild(paRuleNode);
				log.debug("Protected Area '" + paId
						+ "' grants the FireWall rule " + rule + ".");
			}
			LibVirtCloud.conf.store();
			// update related network filter's content
			LibVirtCloudFireWall.authorizeFireWallRules(cnx, paId,
					expand(rules));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static void createTcpUdpRuleElement(Element paRuleNode,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		paRuleNode.setAttribute("from-ip", rule.getFromAddress().toString());
		paRuleNode
				.setAttribute("from-port", rule.getFromPortRange().toString());

		paRuleNode.setAttribute("to-ip", rule.getToAddress().toString());
		paRuleNode.setAttribute("to-port", rule.getToPortRange().toString());

		paRuleNode.setAttribute("direction", rule.getDirection().toString());
		paRuleNode.setAttribute("access", rule.getAccess().toString());
	}

	private static void createIcmpRuleElement(Element paRuleNode,
			SimpleIcmpFireWallRule rule) {
		paRuleNode.setAttribute("from-ip", rule.getFromAddress().toString());

		paRuleNode.setAttribute("to-ip", rule.getToAddress().toString());

		paRuleNode.setAttribute("code", rule.getCode().toString());
		paRuleNode.setAttribute("type", rule.getType().toString());

		paRuleNode.setAttribute("direction", rule.getDirection().toString());
		paRuleNode.setAttribute("access", rule.getAccess().toString());
	}

	public static void revokeFireWallRules(Domain d, NetworkDeviceName devname,
			FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		try {
			String sInstanceId = d.getName();
			Connect cnx = d.getConnect();
			ProtectedAreaId paId = LibVirtCloudNetwork.getSelftProtectedAreaId(
					d, devname);
			Element paRulesNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']/rules");
			for (SimpleFireWallRule rule : rules) {
				Element paRuleNode = null;
				switch (rule.getProtocol()) {
				case TCP:
					paRuleNode = selectTcpUdpFwRuleElement(paRulesNode,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					paRuleNode = selectTcpUdpFwRuleElement(paRulesNode,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					paRuleNode = selectIcmpFwRuleElement(paRulesNode,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				if (paRuleNode == null) {
					continue;
				}
				paRuleNode.getParentNode().removeChild(paRuleNode);
				log.debug("Domain '" + sInstanceId + "' revokes '" + devname
						+ "' the FireWall rule " + rule + ".");
			}
			LibVirtCloud.conf.store();
			// update related network filter's content
			LibVirtCloudFireWall.revokeFireWallRules(cnx, paId, expand(rules));
		} catch (XPathExpressionException | LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void revokeFireWallRules(Connect cnx, ProtectedAreaId paId,
			FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		if (!protectedAreaExists(cnx, paId)) {
			throw new IllegalArgumentException(paId + ": Not accepted. "
					+ "This Protected Area doesn't exists!");
		}
		try {
			Element paRulesNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId + "']/rules");
			for (SimpleFireWallRule rule : rules) {
				Element paRuleNode = null;
				switch (rule.getProtocol()) {
				case TCP:
					paRuleNode = selectTcpUdpFwRuleElement(paRulesNode,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					paRuleNode = selectTcpUdpFwRuleElement(paRulesNode,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					paRuleNode = selectIcmpFwRuleElement(paRulesNode,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				if (paRuleNode == null) {
					continue;
				}
				paRuleNode.getParentNode().removeChild(paRuleNode);
				log.debug("Protected Area '" + paId
						+ "' revokes the FireWall rule " + rule + ".");
			}
			LibVirtCloud.conf.store();
			// update related network filter's content
			LibVirtCloudFireWall.revokeFireWallRules(cnx, paId, expand(rules));
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectTcpUdpFwRuleElement(Element paRulesNode,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		try {
			return (Element) XPathExpander.evaluateAsNode(
					"./" + rule.getProtocol().getValue() + "[@from-ip='"
							+ rule.getFromAddress() + "'   and @from-port='"
							+ rule.getFromPortRange() + "' and @to-ip='"
							+ rule.getToAddress() + "'     and @to-port='"
							+ rule.getToPortRange() + "'   and @access='"
							+ rule.getAccess() + "'        and @direction='"
							+ rule.getDirection() + "']", paRulesNode);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectIcmpFwRuleElement(Element paRulesNode,
			SimpleIcmpFireWallRule rule) {
		try {
			return (Element) XPathExpander.evaluateAsNode(
					"./" + rule.getProtocol().getValue() + "[@from-ip='"
							+ rule.getFromAddress() + "' and @to-ip='"
							+ rule.getToAddress() + "'   and @code='"
							+ rule.getCode() + "'        and @type='"
							+ rule.getType() + "'        and @access='"
							+ rule.getAccess() + "'      and @direction='"
							+ rule.getDirection() + "']", paRulesNode);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static FireWallRules expand(FireWallRules rulesToExpand) {
		FireWallRules expandedRules = new FireWallRules();
		if (rulesToExpand == null) {
			return expandedRules;
		}
		for (SimpleFireWallRule rule : rulesToExpand) {
			expandedRules.addAll(expand(rule));
		}
		return expandedRules;
	}

	private static FireWallRules expand(SimpleFireWallRule ruleToExpand) {
		FireWallRules expandedRules = new FireWallRules();
		for (SimpleFireWallRule rule : expandFrom(ruleToExpand)) {
			expandedRules.addAll(expandTo(rule));
		}
		return expandedRules;
	}

	private static FireWallRules expandFrom(SimpleFireWallRule ruleToExpand) {
		FireWallRules expandedRules = new FireWallRules();
		if (ruleToExpand == null) {
			return expandedRules;
		}
		if (!(ruleToExpand.getFromAddress() instanceof ProtectedAreaId)) {
			expandedRules.add(ruleToExpand);
			return expandedRules;
		}
		ProtectedAreaId paId = (ProtectedAreaId) ruleToExpand.getFromAddress();
		// get the ip of all domains registered in the protected area
		IpRanges ips = getDomainsIps(paId);
		for (IpRange ip : ips) {
			SimpleFireWallRule rule = null;
			switch (ruleToExpand.getProtocol()) {
			case TCP: {
				SimpleTcpFireWallRule tmp = (SimpleTcpFireWallRule) ruleToExpand;
				rule = new SimpleTcpFireWallRule(ip, tmp.getFromPortRange(),
						tmp.getToAddress(), tmp.getToPortRange(),
						tmp.getDirection(), tmp.getAccess());
			}
				break;
			case UDP: {
				SimpleUdpFireWallRule tmp = (SimpleUdpFireWallRule) ruleToExpand;
				rule = new SimpleUdpFireWallRule(ip, tmp.getFromPortRange(),
						tmp.getToAddress(), tmp.getToPortRange(),
						tmp.getDirection(), tmp.getAccess());
			}
				break;
			case ICMP: {
				SimpleIcmpFireWallRule tmp = (SimpleIcmpFireWallRule) ruleToExpand;
				rule = new SimpleIcmpFireWallRule(ip, tmp.getToAddress(),
						tmp.getType(), tmp.getCode(), tmp.getDirection(),
						tmp.getAccess());
			}
				break;
			}
			expandedRules.add(rule);
		}
		return expandedRules;
	}

	private static FireWallRules expandTo(SimpleFireWallRule ruleToExpand) {
		FireWallRules expandedRules = new FireWallRules();
		if (ruleToExpand == null) {
			return expandedRules;
		}
		if (!(ruleToExpand.getToAddress() instanceof ProtectedAreaId)) {
			expandedRules.add(ruleToExpand);
			return expandedRules;
		}
		ProtectedAreaId paId = (ProtectedAreaId) ruleToExpand.getToAddress();
		// get the ip of all domains registered in the protected area
		IpRanges ips = getDomainsIps(paId);
		for (IpRange ip : ips) {
			SimpleFireWallRule rule = null;
			switch (ruleToExpand.getProtocol()) {
			case TCP: {
				SimpleTcpFireWallRule tmp = (SimpleTcpFireWallRule) ruleToExpand;
				rule = new SimpleTcpFireWallRule(tmp.getFromAddress(),
						tmp.getFromPortRange(), ip, tmp.getToPortRange(),
						tmp.getDirection(), tmp.getAccess());
			}
				break;
			case UDP: {
				SimpleUdpFireWallRule tmp = (SimpleUdpFireWallRule) ruleToExpand;
				rule = new SimpleUdpFireWallRule(tmp.getFromAddress(),
						tmp.getFromPortRange(), ip, tmp.getToPortRange(),
						tmp.getDirection(), tmp.getAccess());
			}
				break;
			case ICMP: {
				SimpleIcmpFireWallRule tmp = (SimpleIcmpFireWallRule) ruleToExpand;
				rule = new SimpleIcmpFireWallRule(tmp.getFromAddress(), ip,
						tmp.getType(), tmp.getCode(), tmp.getDirection(),
						tmp.getAccess());
			}
				break;
			}
			expandedRules.add(rule);
		}
		return expandedRules;
	}

	private static IpRanges getDomainsIps(ProtectedAreaId paId) {
		IpRanges ips = new IpRanges();
		if (paId == null) {
			return ips;
		}
		try {
			NodeList domainNodes = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area[@id='" + paId
							+ "']/domains/domain");
			if (domainNodes == null || domainNodes.getLength() == 0) {
				return ips;
			}
			for (int i = 0; i < domainNodes.getLength(); i++) {
				Element domainNode = (Element) domainNodes.item(i);
				String sMacAddr = domainNode.getAttribute("mac-addr");
				String sIpAddr = LibVirtCloudNetwork
						.getDomainIpAddress(sMacAddr);
				ips.add(IpRange.parseString(sIpAddr));
			}
		} catch (XPathExpressionException | IllegalIpRangeException Ex) {
			throw new RuntimeException(Ex);
		}
		return ips;
	}

	private static void updateAllProtectedAreasRelatedTo(Connect cnx,
			ProtectedAreaId paId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (paId == null) {
			return;
		}
		// search all protected area related to the given one
		try {
			NodeList paNodes = LibVirtCloud.conf
					.evaluateAsNodeList("/libvirtcloud/protected-areas/"
							+ "protected-area[ rules/*/@from-ip='" + paId
							+ "' or rules/*/to-ip='" + paId + "' ]");
			if (paNodes == null || paNodes.getLength() == 0) {
				return;
			}
			for (int i = 0; i < paNodes.getLength(); i++) {
				Element paNode = (Element) paNodes.item(i);
				String sPaId = paNode.getAttribute("id");
				ProtectedAreaId id = ProtectedAreaId.parseString(sPaId);
				updateProtectedArea(cnx, id);
			}
		} catch (XPathExpressionException | IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static void updateProtectedArea(Connect cnx, ProtectedAreaId paId) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		if (paId == null) {
			return;
		}
		// get the protected area fire wall rules
		FireWallRules rules = getFireWallRules(cnx, paId);
		// expand the fire wall rules
		rules = expand(rules);
		// store the fire wall rules
		LibVirtCloudFireWall.setFireWallRules(cnx, paId, rules);
	}

}