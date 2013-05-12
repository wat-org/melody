package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.Interfaces;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.exception.IllegalAccessException;
import com.wat.melody.common.firewall.exception.IllegalDirectionsException;
import com.wat.melody.common.firewall.exception.IllegalInterfacesException;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FwRule Node, which define the name of the device to
	 * attache the Fw Rule to.
	 */
	public static final String DEVICES_NAME_ATTR = "devices-name";

	/**
	 * XML attribute of a FwRule Node, which define the source ips of the Fw
	 * Rule.
	 */
	public static final String FROM_IPS_ATTR = "from-ips";

	/**
	 * XML attribute of a FwRule Node, which define the destination ips of the
	 * Fw Rule.
	 */
	public static final String TO_IPS_ATTR = "to-ips";

	/**
	 * XML attribute of a FwRule Node, which define the directions of the flow.
	 */
	public static final String DIRECTIONS_ATTR = "directions";

	/**
	 * XML attribute of a FwRule Node, which define the action to perform.
	 */
	public static final String ACCESS_ATTR = "access";

	protected Interfaces loadInterfaces(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DEVICES_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return Interfaces.ALL;
		}
		try {
			return Interfaces.parseString(v);
		} catch (IllegalInterfacesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					DEVICES_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	protected IpRanges loadFromIps(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, FROM_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, FROM_IPS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	protected IpRanges loadToIps(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TO_IPS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return IpRanges.parseString(v);
		} catch (IllegalIpRangesException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, TO_IPS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	protected Directions loadDirection(Node n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, DIRECTIONS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Directions.parseString(v);
		} catch (IllegalDirectionsException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					DIRECTIONS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	protected Access loadAccess(Node n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, ACCESS_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Access.parseString(v);
		} catch (IllegalAccessException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n, ACCESS_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	public abstract FireWallRulesPerDevice load(Node instanceNode)
			throws ResourcesDescriptorException;

}
