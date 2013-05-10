package com.wat.cloud.libvirt;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.firewall.AbstractTcpUdpFwRuleDecomposed;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FwRuleDecomposed;
import com.wat.melody.common.firewall.FwRulesDecomposed;
import com.wat.melody.common.firewall.IcmpCode;
import com.wat.melody.common.firewall.IcmpFwRuleDecomposed;
import com.wat.melody.common.firewall.IcmpType;
import com.wat.melody.common.firewall.Interface;
import com.wat.melody.common.firewall.Protocol;
import com.wat.melody.common.firewall.TcpFwRuleDecomposed;
import com.wat.melody.common.firewall.UdpFwRuleDecomposed;
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

	public static FwRulesDecomposed getFireWallRules(Domain d,
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
			FwRulesDecomposed rules = new FwRulesDecomposed();
			Connect cnx = d.getConnect();
			String sSGName = LibVirtCloud.getSecurityGroup(d, netdev);
			NetworkFilter nf = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(nf.getXMLDesc());

			NodeList nl = doc.evaluateAsNodeList("/filter/rule");
			Node n = null;
			Interface inter = Interface.parseString(netdev.getValue());
			for (int i = 0; i < nl.getLength(); i++) {
				n = nl.item(i);
				String sProtocol = XPathExpander.evaluateAsString(
						"./node-name(*)", n);
				Protocol proto = Protocol.parseString(sProtocol);
				FwRuleDecomposed rule = null;
				switch (proto) {
				case TCP:
					rule = createTcpRuleFromNode(inter, n);
					break;
				case UDP:
					rule = createUdpRuleFromNode(inter, n);
					break;
				case ICMP:
					rule = createIcmpRuleFromNode(inter, n);
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

	private static FwRuleDecomposed createTcpRuleFromNode(Interface inter,
			Node n) {
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
			return new TcpFwRuleDecomposed(inter, fromIp, fromPorts, toIp,
					toPorts, dir, access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalPortRangeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static FwRuleDecomposed createUdpRuleFromNode(Interface inter,
			Node n) {
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
			return new UdpFwRuleDecomposed(inter, fromIp, fromPorts, toIp,
					toPorts, dir, access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalPortRangeException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static FwRuleDecomposed createIcmpRuleFromNode(Interface inter,
			Node n) {
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

			return new IcmpFwRuleDecomposed(inter, fromIp, toIp, type, code,
					dir, access);
		} catch (XPathExpressionException | IllegalIpRangeException
				| IllegalIcmpTypeException | IllegalIcmpCodeException Ex) {
			throw new RuntimeException(Ex);
		}
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
			String sSGName = LibVirtCloud.getSecurityGroup(d, netdev);
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(sg.getXMLDesc());

			for (FwRuleDecomposed rule : rules) {
				Node n = null;
				switch (rule.getProtocol()) {
				case TCP:
					n = selectTcpUdpFwRuleNode(doc, (TcpFwRuleDecomposed) rule);
					break;
				case UDP:
					n = selectTcpUdpFwRuleNode(doc, (UdpFwRuleDecomposed) rule);
					break;
				case ICMP:
					n = selectIcmpFwRuleNode(doc, (IcmpFwRuleDecomposed) rule);
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

	private static Node selectTcpUdpFwRuleNode(Doc doc,
			AbstractTcpUdpFwRuleDecomposed rule) {
		try {
			return doc.evaluateAsNode("/filter/rule[" + " @action='"
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

	private static Node selectIcmpFwRuleNode(Doc doc, IcmpFwRuleDecomposed rule) {
		try {
			String typeCond = " and not(exists(@type))";
			String codeCond = " and not(exists(@code))";
			if (!rule.getType().equals(IcmpType.ALL)) {
				typeCond = " and @type='" + rule.getType() + "'";
			}
			if (!rule.getCode().equals(IcmpCode.ALL)) {
				codeCond = " and @code='" + rule.getCode() + "'";
			}
			return doc.evaluateAsNode("/filter/rule[" + " @action='"
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
			String sSGName = LibVirtCloud.getSecurityGroup(d, netdev);
			NetworkFilter sg = cnx.networkFilterLookupByName(sSGName);
			Doc doc = new Doc();
			doc.loadFromXML(sg.getXMLDesc());

			for (FwRuleDecomposed rule : rules) {
				Node nrule = doc.getDocument().createElement("rule");
				Node nin = null;
				switch (rule.getProtocol()) {
				case TCP:
					nin = createTcpUdpRuleNode(nrule,
							(TcpFwRuleDecomposed) rule);
					break;
				case UDP:
					nin = createTcpUdpRuleNode(nrule,
							(UdpFwRuleDecomposed) rule);
					break;
				case ICMP:
					nin = createIcmpRuleNode(nrule, (IcmpFwRuleDecomposed) rule);
					break;
				}
				if (nin == null) {
					continue;
				}
				Doc.createAttribute("priority", "500", nrule);
				Doc.createAttribute("action",
						rule.getAccess() == Access.ALLOW ? "accept" : "drop",
						nrule);
				Doc.createAttribute("direction",
						rule.getDirection() == Direction.IN ? "in" : "out",
						nrule);
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

	private static Node createTcpUdpRuleNode(Node nrule,
			AbstractTcpUdpFwRuleDecomposed rule) {
		Node n = nrule.getOwnerDocument().createElement(
				rule.getProtocol().getValue());
		Doc.createAttribute("state", "NEW", n);

		Doc.createAttribute("srcipaddr", rule.getFromIpRange().getIp(), n);
		Doc.createAttribute("srcipmask", rule.getFromIpRange().getMask(), n);

		Doc.createAttribute("srcportstart", rule.getFromPortRange()
				.getStartPort().toString(), n);
		Doc.createAttribute("srcportend", rule.getFromPortRange().getEndPort()
				.toString(), n);

		Doc.createAttribute("dstipaddr", rule.getToIpRange().getIp(), n);
		Doc.createAttribute("dstipmask", rule.getToIpRange().getMask(), n);

		Doc.createAttribute("dstportstart", rule.getToPortRange()
				.getStartPort().toString(), n);
		Doc.createAttribute("dstportend", rule.getToPortRange().getEndPort()
				.toString(), n);
		return n;
	}

	private static Node createIcmpRuleNode(Node nrule, IcmpFwRuleDecomposed rule) {
		Node n = nrule.getOwnerDocument().createElement("icmp");
		Doc.createAttribute("state", "NEW", n);

		Doc.createAttribute("srcipaddr", rule.getFromIpRange().getIp(), n);
		Doc.createAttribute("srcipmask", rule.getFromIpRange().getMask(), n);

		Doc.createAttribute("dstipaddr", rule.getToIpRange().getIp(), n);
		Doc.createAttribute("dstipmask", rule.getToIpRange().getMask(), n);

		if (!rule.getType().equals(IcmpType.ALL)) {
			Doc.createAttribute("type", rule.getType().toString(), n);
		}
		if (!rule.getCode().equals(IcmpCode.ALL)) {
			Doc.createAttribute("code", rule.getCode().toString(), n);
		}
		return n;
	}

}