package com.wat.melody.cloud.firewall;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.FwRule;
import com.wat.melody.common.network.FwRules;
import com.wat.melody.common.network.Interfaces;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.Protocols;
import com.wat.melody.common.network.exception.IllegalAccessException;
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
	 * The 'fwrule' XML Nested element of the Instance Node in the RD
	 */
	public static final String FIREWALL_RULE_NE = "fwrule";

	/**
	 * The 'device' XML attribute of a FwRule Node
	 */
	public static final String DEVICE_ATTR = "device";

	/**
	 * The 'from' XML attribute of a FwRule Node
	 */
	public static final String FROM_ATTR = "from";

	/**
	 * The 'ports' XML attribute of a FwRule Node
	 */
	public static final String PORTS_ATTR = "ports";

	/**
	 * The 'protocols' XML attribute of a FwRule Node
	 */
	public static final String PROTOCOLS_ATTR = "protocols";

	/**
	 * The 'access' XML attribute of a FwRule Node
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

	private boolean loadFrom(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, FROM_ATTR);
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

	private boolean loadDevices(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, DEVICE_ATTR);
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

	private boolean loadPorts(Node n, FwRule fw)
			throws ResourcesDescriptorException {
		Node attr = XPathHelper.getHeritedAttribute(n, PORTS_ATTR);
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
			fw.setPortRanges(PortRanges.parseString(v));
		} catch (IllegalPortRangesException Ex) {
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
	 * <li>from : which should contains {@link IpRanges} ;</li>
	 * <li>to : which should contains {@link IpRanges} :</li>
	 * <li>ports : which should contains {@link PortRanges} ;</li>
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
			if (!loadFrom(n, fw)) {
				continue;
			}
			loadDevices(n, fw);
			loadPorts(n, fw);
			loadProtocols(n, fw);
			loadAccess(n, fw);
			fwrs.add(fw);
		}
		return fwrs;
	}

}