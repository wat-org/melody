package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.Directions;
import com.wat.melody.common.network.FwRule;
import com.wat.melody.common.network.FwRules;
import com.wat.melody.common.network.Interfaces;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.Protocols;
import com.wat.melody.common.network.exception.IllegalAccessException;
import com.wat.melody.common.network.exception.IllegalDirectionsException;
import com.wat.melody.common.network.exception.IllegalInterfacesException;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.network.exception.IllegalProtocolsException;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FwRuleLoader {

	/**
	 * XML Nested element of an Instance Node, which contains the definition of
	 * a FwRule.
	 */
	public static final String FIREWALL_RULE_NE = "fwrule";

	/**
	 * XML attribute of a FwRule Node which define the name of the device to
	 * attache the Fw Rule to.
	 */
	public static final String DEVICES_NAME_ATTR = "devices-name";

	/**
	 * XML attribute of a FwRule Node which define the source ips of the Fw
	 * Rule.
	 */
	public static final String FROM_IPS_ATTR = "from-ips";

	/**
	 * XML attribute of a FwRule Node which define the source ports of the Fw
	 * Rule.
	 */
	public static final String FROM_PORTS_ATTR = "from-ports";

	/**
	 * XML attribute of a FwRule Node which define the destination ips of the Fw
	 * Rule.
	 */
	public static final String TO_IPS_ATTR = "to-ips";

	/**
	 * XML attribute of a FwRule Node which define the destination ports of the
	 * Fw Rule.
	 */
	public static final String TO_PORTS_ATTR = "to-ports";

	/**
	 * XML attribute of a FwRule Node which define the protocols of the Fw Rule.
	 */
	public static final String PROTOCOLS_ATTR = "protocols";

	/**
	 * XML attribute of a FwRule Node which define the direction of the flow.
	 */
	public static final String DIRECTION_ATTR = "direction";

	/**
	 * XML attribute of a FwRule Node which define the action to perform.
	 */
	public static final String ACCESS_ATTR = "access";

	private ITaskContext moTC;

	public FwRuleLoader(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moTC = tc;
	}

	protected ITaskContext getTC() {
		return moTC;
	}

	private boolean loadFromIps(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, FROM_IPS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setFromIpRanges(IpRanges.parseString(v));
		} catch (IllegalIpRangesException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadFromPorts(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, FROM_PORTS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setFromPortRanges(PortRanges.parseString(v));
		} catch (IllegalPortRangesException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadToIps(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, TO_IPS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setToIpRanges(IpRanges.parseString(v));
		} catch (IllegalIpRangesException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadToPorts(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, TO_PORTS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setToPortRanges(PortRanges.parseString(v));
		} catch (IllegalPortRangesException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadDevices(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, DEVICES_NAME_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setInterfaces(Interfaces.parseString(v));
		} catch (IllegalInterfacesException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadProtocols(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, PROTOCOLS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setProtocols(Protocols.parseString(v));
		} catch (IllegalProtocolsException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadDirection(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, DIRECTION_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setDirections(Directions.parseString(v));
		} catch (IllegalDirectionsException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	private boolean loadAccess(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, ACCESS_ATTR);
		if (attr == null) {
			return false;
		}
		String v = attr.getNodeValue();
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setAccess(Access.parseString(v));
		} catch (IllegalAccessException Ex) {
			throw new ResourcesDescriptorException(attr, Ex);
		}
		return true;
	}

	/**
	 * <p>
	 * Converts the given FwRule <code>Node</code>s into {@link FwRules}.
	 * </p>
	 * 
	 * <p>
	 * <i>A FwRule <code>Node</code> must have the attributes :
	 * <ul>
	 * <li>device-name : which should contains {@link NetworkDeviceName} ;</li>
	 * <li>from-ips : which should contains {@link IpRanges} ;</li>
	 * <li>from-ports : which should contains {@link PortRanges} ;</li>
	 * <li>to-ips : which should contains {@link IpRanges} :</li>
	 * <li>to-ports : which should contains {@link PortRanges} ;</li>
	 * <li>protocols : which should contains {@link Protocols} ;</li>
	 * <li>allow : which should contains {@link Access} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another FwRule <code>Node</code>, which attributes will be used as source
	 * ;</li>
	 * </ul>
	 * </i>
	 * </p>
	 * 
	 * @param nl
	 *            a list of FwRule <code>Node</code>s.
	 * 
	 * @return a {@link FwRules} object, which is a collection of {@link FwRule}
	 *         .
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the conversion failed (ex : the content of a FwRule Node's
	 *             attribute is not valid, or the 'herit' XML attribute is not
	 *             valid).
	 */
	public FwRules load(NodeList nl) throws ResourcesDescriptorException {
		FwRules fwrs = new FwRules();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			FwRule fw = new FwRule();
			if (!loadFromIps(n, fw)) {
				continue;
			}
			loadFromPorts(n, fw);
			loadToIps(n, fw);
			loadToPorts(n, fw);
			loadDevices(n, fw);
			loadProtocols(n, fw);
			loadDirection(n, fw);
			loadAccess(n, fw);
			fwrs.add(fw);
		}
		return fwrs;
	}

}