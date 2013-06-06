package com.wat.melody.cloud.firewall;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.firewall.exception.IllegalAccessException;
import com.wat.melody.common.firewall.exception.IllegalDirectionsException;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefsException;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FwRule Element Node, which define the name of the
	 * device to attache the Fw Rule to.
	 */
	public static final String DEVICES_NAME_ATTR = "devices-name";

	/**
	 * XML attribute of a FwRule Element Node, which define the source ips of
	 * the Fw Rule.
	 */
	public static final String FROM_IPS_ATTR = "from-ips";

	/**
	 * XML attribute of a FwRule Element Node, which define the destination ips
	 * of the Fw Rule.
	 */
	public static final String TO_IPS_ATTR = "to-ips";

	/**
	 * XML attribute of a FwRule Element Node, which define the directions of
	 * the flow.
	 */
	public static final String DIRECTIONS_ATTR = "directions";

	/**
	 * XML attribute of a FwRule Element Node, which define the action to
	 * perform.
	 */
	public static final String ACCESS_ATTR = "access";

	protected NetworkDeviceNameRefs loadNetworkDeviceNameRefs(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, DEVICES_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return NetworkDeviceNameRefs.ALL;
		}
		try {
			return NetworkDeviceNameRefs.parseString(v);
		} catch (IllegalNetworkDeviceNameRefsException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					DEVICES_NAME_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected IpRanges loadFromIps(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, FROM_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e, FROM_IPS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected IpRanges loadToIps(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TO_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e, TO_IPS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected Directions loadDirection(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, DIRECTIONS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Directions.parseString(v);
		} catch (IllegalDirectionsException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					DIRECTIONS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	protected Access loadAccess(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, ACCESS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Access.parseString(v);
		} catch (IllegalAccessException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e, ACCESS_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	public abstract FireWallRulesPerDevice load(Element instanceElement)
			throws NodeRelatedException;

}