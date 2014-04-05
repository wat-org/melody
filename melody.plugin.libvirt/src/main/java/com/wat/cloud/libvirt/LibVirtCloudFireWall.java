package com.wat.cloud.libvirt;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.IcmpCode;
import com.wat.melody.common.firewall.IcmpType;
import com.wat.melody.common.firewall.SimpleAbstractTcpUdpFireWallwRule;
import com.wat.melody.common.firewall.SimpleFireWallRule;
import com.wat.melody.common.firewall.SimpleIcmpFireWallRule;
import com.wat.melody.common.firewall.SimpleTcpFireWallRule;
import com.wat.melody.common.firewall.SimpleUdpFireWallRule;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.xml.Doc;

/**
 * <p>
 * Quick and dirty class which provides libvirt firewall management features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudFireWall {

	protected static void setFireWallRules(Connect cnx, ProtectedAreaId paId,
			FireWallRules rules) {
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
		if (rules == null) {
			rules = new FireWallRules();
		}
		try {
			// load current rules
			boolean changed = false;
			String sgid = paId.getValue();
			NetworkFilter sg = cnx.networkFilterLookupByName(sgid);
			Doc doc = new Doc();
			doc.loadFromXML(sg.getXMLDesc());
			// remove all current rules
			NodeList ruleNodes = doc.evaluateAsNodeList("/filter/rule");
			if (ruleNodes != null && ruleNodes.getLength() > 0) {
				for (int i = 0; i < ruleNodes.getLength(); i++) {
					Element ruleNode = (Element) ruleNodes.item(i);
					ruleNode.getParentNode().removeChild(ruleNode);
				}
				changed = true;
			}

			// insert all new rules
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
				changed = true;
			}
			if (changed) {
				cnx.networkFilterDefineXML(doc.dump());
			}
		} catch (XPathExpressionException | LibvirtException | MelodyException
				| IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void revokeFireWallRules(Connect cnx,
			ProtectedAreaId paId, FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
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
			boolean changed = false;
			String sgid = paId.getValue();
			NetworkFilter sg = cnx.networkFilterLookupByName(sgid);
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
					n = selectIcmpFwRuleElement(doc,
							(SimpleIcmpFireWallRule) rule);
					break;
				}
				if (n == null) {
					continue;
				}
				n.getParentNode().removeChild(n);
				changed = true;
			}
			if (changed) {
				cnx.networkFilterDefineXML(doc.dump());
			}
		} catch (LibvirtException | MelodyException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectTcpUdpFwRuleElement(Doc doc,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		// this Address can't be a ProtectedAreaId
		IpRange fromIpRange = (IpRange) rule.getFromAddress();
		IpRange toIpRange = (IpRange) rule.getToAddress();

		try {
			return (Element) doc.evaluateAsNode("/filter/rule[" + " @action='"
					+ (rule.getAccess() == Access.ALLOW ? "accept" : "drop")
					+ "' and @direction='"
					+ (rule.getDirection() == Direction.IN ? "in" : "out")
					+ "' and exists(" + rule.getProtocol().getValue() + "["
					+ "@srcipaddr='" + fromIpRange.getIp()
					+ "' and @srcipmask='" + fromIpRange.getMask()
					+ "' and @srcportstart='"
					+ rule.getFromPortRange().getStartPort()
					+ "' and @srcportend='"
					+ rule.getFromPortRange().getEndPort()
					+ "' and @dstipaddr='" + toIpRange.getIp()
					+ "' and @dstipmask='" + toIpRange.getMask()
					+ "' and @dstportstart='"
					+ rule.getToPortRange().getStartPort()
					+ "' and @dstportend='"
					+ rule.getToPortRange().getEndPort() + "'])]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element selectIcmpFwRuleElement(Doc doc,
			SimpleIcmpFireWallRule rule) {
		// this Address can't be a ProtectedAreaId
		IpRange fromIpRange = (IpRange) rule.getFromAddress();
		IpRange toIpRange = (IpRange) rule.getToAddress();

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
					+ fromIpRange.getIp() + "' and @srcipmask='"
					+ fromIpRange.getMask() + "' and @dstipaddr='"
					+ toIpRange.getIp() + "' and @dstipmask='"
					+ toIpRange.getMask() + "'" + typeCond + codeCond + "])]");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void authorizeFireWallRules(Connect cnx,
			ProtectedAreaId paId, FireWallRules rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
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
			boolean changed = false;
			String sgid = paId.getValue();
			NetworkFilter sg = cnx.networkFilterLookupByName(sgid);
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
				changed = true;
			}
			if (changed) {
				cnx.networkFilterDefineXML(doc.dump());
			}
		} catch (LibvirtException | MelodyException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Element createTcpUdpRuleElement(Element nrule,
			SimpleAbstractTcpUdpFireWallwRule rule) {
		// this Address can't be a ProtectedAreaId
		IpRange fromIpRange = (IpRange) rule.getFromAddress();
		IpRange toIpRange = (IpRange) rule.getToAddress();

		Element n = nrule.getOwnerDocument().createElement(
				rule.getProtocol().getValue());
		n.setAttribute("state", "NEW");

		n.setAttribute("srcipaddr", fromIpRange.getIp());
		n.setAttribute("srcipmask", fromIpRange.getMask());

		n.setAttribute("srcportstart", rule.getFromPortRange().getStartPort()
				.toString());
		n.setAttribute("srcportend", rule.getFromPortRange().getEndPort()
				.toString());

		n.setAttribute("dstipaddr", toIpRange.getIp());
		n.setAttribute("dstipmask", toIpRange.getMask());

		n.setAttribute("dstportstart", rule.getToPortRange().getStartPort()
				.toString());
		n.setAttribute("dstportend", rule.getToPortRange().getEndPort()
				.toString());
		return n;
	}

	private static Element createIcmpRuleElement(Element nrule,
			SimpleIcmpFireWallRule rule) {
		// this Address can't be a ProtectedAreaId
		IpRange fromIpRange = (IpRange) rule.getFromAddress();
		IpRange toIpRange = (IpRange) rule.getToAddress();

		Element n = nrule.getOwnerDocument().createElement("icmp");
		n.setAttribute("state", "NEW");

		n.setAttribute("srcipaddr", fromIpRange.getIp());
		n.setAttribute("srcipmask", fromIpRange.getMask());

		n.setAttribute("dstipaddr", toIpRange.getIp());
		n.setAttribute("dstipmask", toIpRange.getMask());

		if (!rule.getType().equals(IcmpType.ALL)) {
			n.setAttribute("type", rule.getType().toString());
		}
		if (!rule.getCode().equals(IcmpCode.ALL)) {
			n.setAttribute("code", rule.getCode().toString());
		}
		return n;
	}

}