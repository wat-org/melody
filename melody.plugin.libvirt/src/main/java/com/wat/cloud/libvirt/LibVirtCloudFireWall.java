package com.wat.cloud.libvirt;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * <p>
 * Quick and dirty class which provide libvirt firewall management features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudFireWall {

	private static Log log = LogFactory.getLog(LibVirtCloudFireWall.class);

	public static FireWallRules getFireWallRules(Domain d,
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
		try {
			FireWallRules rules = new FireWallRules();
			Connect cnx = d.getConnect();
			String sSGName = LibVirtCloudNetwork.getSecurityGroup(d, netdev);
			NetworkFilter nf = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(nf.getXMLDesc());

			NodeList nl = doc.evaluateAsNodeList("/filter/rule");
			Element n = null;
			for (int i = 0; i < nl.getLength(); i++) {
				n = (Element) nl.item(i);
				String sProtocol = XPathExpander.evaluateAsString(
						"./node-name(*)", n);
				Protocol proto = Protocol.parseString(sProtocol);
				SimpleFireWallRule rule = null;
				switch (proto) {
				case TCP:
					rule = createTcpRuleFromElement(n);
					break;
				case UDP:
					rule = createUdpRuleFromElement(n);
					break;
				case ICMP:
					rule = createIcmpRuleFromElement(n);
					break;
				}
				if (rule == null) {
					continue;
				}
				rules.add(rule);
			}
			return rules;
		} catch (LibvirtException | IOException | XPathExpressionException
				| MelodyException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static SimpleFireWallRule createTcpRuleFromElement(Element n) {
		try {
			String sIp = XPathExpander.evaluateAsString("./*/@srcipaddr", n);
			String sMask = XPathExpander.evaluateAsString("./*/@srcipmask", n);
			IpRange fromIp = IpRange.parseString(sIp + "/" + sMask);

			String start = XPathExpander
					.evaluateAsString("./*/@srcporstart", n);
			String end = XPathExpander.evaluateAsString("./*/@srcportend", n);
			PortRange fromPorts = PortRange.parseString(start + "-" + end);

			sIp = XPathExpander.evaluateAsString("./*/@dstipaddr", n);
			sMask = XPathExpander.evaluateAsString("./*/@dstipmask", n);
			IpRange toIp = IpRange.parseString(sIp + "/" + sMask);

			start = XPathExpander.evaluateAsString("./*/@dstportstart", n);
			end = XPathExpander.evaluateAsString("./*/@dstportend", n);
			PortRange toPorts = PortRange.parseString(start + "-" + end);

			String sDir = XPathExpander.evaluateAsString("./@direction", n);
			Direction dir = sDir.equalsIgnoreCase("in") ? Direction.IN
					: Direction.OUT;

			String sAccess = XPathExpander.evaluateAsString("./@action", n);
			Access access = sAccess.equalsIgnoreCase("accept") ? Access.ALLOW
					: Access.DENY;
			return new SimpleTcpFireWallRule(fromIp, fromPorts, toIp, toPorts,
					dir, access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalPortRangeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static SimpleFireWallRule createUdpRuleFromElement(Element n) {
		try {
			String sIp = XPathExpander.evaluateAsString("./*/@srcipaddr", n);
			String sMask = XPathExpander.evaluateAsString("./*/@srcipmask", n);
			IpRange fromIp = IpRange.parseString(sIp + "/" + sMask);

			String start = XPathExpander
					.evaluateAsString("./*/@srcporstart", n);
			String end = XPathExpander.evaluateAsString("./*/@srcportend", n);
			PortRange fromPorts = PortRange.parseString(start + "-" + end);

			sIp = XPathExpander.evaluateAsString("./*/@dstipaddr", n);
			sMask = XPathExpander.evaluateAsString("./*/@dstipmask", n);
			IpRange toIp = IpRange.parseString(sIp + "/" + sMask);

			start = XPathExpander.evaluateAsString("./*/@dstportstart", n);
			end = XPathExpander.evaluateAsString("./*/@dstportend", n);
			PortRange toPorts = PortRange.parseString(start + "-" + end);

			String sDir = XPathExpander.evaluateAsString("./@direction", n);
			Direction dir = sDir.equalsIgnoreCase("in") ? Direction.IN
					: Direction.OUT;

			String sAccess = XPathExpander.evaluateAsString("./@action", n);
			Access access = sAccess.equalsIgnoreCase("accept") ? Access.ALLOW
					: Access.DENY;
			return new SimpleUdpFireWallRule(fromIp, fromPorts, toIp, toPorts,
					dir, access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalPortRangeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static SimpleFireWallRule createIcmpRuleFromElement(Element n) {
		try {
			String sIp = XPathExpander.evaluateAsString("./*/@srcipaddr", n);
			String sMask = XPathExpander.evaluateAsString("./*/@srcipmask", n);
			IpRange fromIp = IpRange.parseString(sIp + "/" + sMask);

			sIp = XPathExpander.evaluateAsString("./*/@dstipaddr", n);
			sMask = XPathExpander.evaluateAsString("./*/@dstipmask", n);
			IpRange toIp = IpRange.parseString(sIp + "/" + sMask);

			String sDir = XPathExpander.evaluateAsString("./@direction", n);
			Direction dir = sDir.equalsIgnoreCase("in") ? Direction.IN
					: Direction.OUT;

			String sAccess = XPathExpander.evaluateAsString("./@action", n);
			Access access = sAccess.equalsIgnoreCase("accept") ? Access.ALLOW
					: Access.DENY;

			String sType = XPathExpander.evaluateAsString("./*/@type", n);
			IcmpType type = IcmpType.ALL;
			if (sType != null && sType.length() != 0) {
				type = IcmpType.parseString(sType);
			}

			String sCode = XPathExpander.evaluateAsString("./*/@code", n);
			IcmpCode code = IcmpCode.ALL;
			if (sCode != null && sCode.length() != 0) {
				code = IcmpCode.parseString(sCode);
			}

			return new SimpleIcmpFireWallRule(fromIp, toIp, type, code, dir,
					access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalIcmpTypeException | IllegalIcmpCodeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void revokeFireWallRules(Domain d, NetworkDeviceName netdev,
			FireWallRules rules) {
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
			String sSGName = LibVirtCloudNetwork.getSecurityGroup(d, netdev);
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(sg.getXMLDesc());

			for (SimpleFireWallRule rule : rules) {
				Element n = null;
				switch (rule.getProtocol()) {
				case TCP:
					n = selectTcpUdpFwRuleElement(doc,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					n = selectTcpUdpFwRuleElement(doc,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					n = selectIcmpFwRuleelement(doc,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				if (n == null) {
					continue;
				}
				n.getParentNode().removeChild(n);
				log.debug("Domain '" + sInstanceId + "' revokes '" + netdev
						+ "' the FireWall rule " + rule + ".");
			}
			cnx.networkFilterDefineXML(doc.dump());
		} catch (LibvirtException | MelodyException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectTcpUdpFwRuleElement(Doc doc,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		try {
			return (Element) doc.evaluateAsNode("/filter/rule[" + " @action='"
					+ (rule.getAccess() == Access.ALLOW ? "accept" : "drop")
					+ "' and @direction='"
					+ (rule.getDirection() == Direction.IN ? "in" : "out")
					+ "' and exists(" + rule.getProtocol().getValue() + "["
					+ "@srcipaddr='" + rule.getFromIpRange().getIp()
					+ "' and @srcipmask='" + rule.getFromIpRange().getMask()
					+ "' and @srcportstart='"
					+ rule.getFromPortRange().getStartPort()
					+ "' and @srcportend='"
					+ rule.getFromPortRange().getEndPort()
					+ "' and @dstipaddr='" + rule.getToIpRange().getIp()
					+ "' and @dstipmask='" + rule.getToIpRange().getMask()
					+ "' and @dstportstart='"
					+ rule.getToPortRange().getStartPort()
					+ "' and @dstportend='"
					+ rule.getToPortRange().getEndPort() + "'])]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectIcmpFwRuleelement(Doc doc,
			SimpleIcmpFireWallRule rule) {
		try {
			String typeCond = " and not(exists(@type))";
			String codeCond = " and not(exists(@code))";
			if (!rule.getType().equals(IcmpType.ALL)) {
				typeCond = " and @type='" + rule.getType() + "'";
			}
			if (!rule.getCode().equals(IcmpCode.ALL)) {
				codeCond = " and @code='" + rule.getCode() + "'";
			}
			return (Element) doc.evaluateAsNode("/filter/rule[" + " @action='"
					+ (rule.getAccess() == Access.ALLOW ? "accept" : "drop")
					+ "' and @direction='"
					+ (rule.getDirection() == Direction.IN ? "in" : "out")
					+ "' and exists(" + "icmp" + "[" + "@srcipaddr='"
					+ rule.getFromIpRange().getIp() + "' and @srcipmask='"
					+ rule.getFromIpRange().getMask() + "' and @dstipaddr='"
					+ rule.getToIpRange().getIp() + "' and @dstipmask='"
					+ rule.getToIpRange().getMask() + "'" + typeCond + codeCond
					+ "])]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public static void authorizeFireWallRules(Domain d,
			NetworkDeviceName netdev, FireWallRules rules) {
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
			String sSGName = LibVirtCloudNetwork.getSecurityGroup(d, netdev);
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(sg.getXMLDesc());

			for (SimpleFireWallRule rule : rules) {
				Element nrule = doc.getDocument().createElement("rule");
				Element nin = null;
				switch (rule.getProtocol()) {
				case TCP:
					nin = createTcpUdpRuleElement(nrule,
							(SimpleTcpFireWallRule) rule);
					break;
				case UDP:
					nin = createTcpUdpRuleElement(nrule,
							(SimpleUdpFireWallRule) rule);
					break;
				case ICMP:
					nin = createIcmpRuleElement(nrule,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				if (nin == null) {
					continue;
				}
				nrule.setAttribute("priority", "500");
				nrule.setAttribute("action",
						rule.getAccess() == Access.ALLOW ? "accept" : "drop");
				nrule.setAttribute("direction",
						rule.getDirection() == Direction.IN ? "in" : "out");
				nrule.appendChild(nin);
				doc.getDocument().getFirstChild().appendChild(nrule);
				log.debug("Domain '" + sInstanceId + "' grants '" + netdev
						+ "' the FireWall rule " + rule + ".");
			}
			cnx.networkFilterDefineXML(doc.dump());
		} catch (LibvirtException | MelodyException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element createTcpUdpRuleElement(Element nrule,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		Element n = nrule.getOwnerDocument().createElement(
				rule.getProtocol().getValue());
		n.setAttribute("state", "NEW");

		n.setAttribute("srcipaddr", rule.getFromIpRange().getIp());
		n.setAttribute("srcipmask", rule.getFromIpRange().getMask());

		n.setAttribute("srcportstart", rule.getFromPortRange().getStartPort()
				.toString());
		n.setAttribute("srcportend", rule.getFromPortRange().getEndPort()
				.toString());

		n.setAttribute("dstipaddr", rule.getToIpRange().getIp());
		n.setAttribute("dstipmask", rule.getToIpRange().getMask());

		n.setAttribute("dstportstart", rule.getToPortRange().getStartPort()
				.toString());
		n.setAttribute("dstportend", rule.getToPortRange().getEndPort()
				.toString());
		return n;
	}

	private static Element createIcmpRuleElement(Element nrule,
			SimpleIcmpFireWallRule rule) {
		Element n = nrule.getOwnerDocument().createElement("icmp");
		n.setAttribute("state", "NEW");

		n.setAttribute("srcipaddr", rule.getFromIpRange().getIp());
		n.setAttribute("srcipmask", rule.getFromIpRange().getMask());

		n.setAttribute("dstipaddr", rule.getToIpRange().getIp());
		n.setAttribute("dstipmask", rule.getToIpRange().getMask());

		if (!rule.getType().equals(IcmpType.ALL)) {
			n.setAttribute("type", rule.getType().toString());
		}
		if (!rule.getCode().equals(IcmpCode.ALL)) {
			n.setAttribute("code", rule.getCode().toString());
		}
		return n;
	}

}