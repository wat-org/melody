package com.wat.melody.plugin.aws.ec2.common;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.FwRule;
import com.wat.melody.common.network.FwRules;
import com.wat.melody.common.network.IpRanges;
import com.wat.melody.common.network.PortRanges;
import com.wat.melody.common.network.Protocols;
import com.wat.melody.common.network.exception.IllegalAccessException;
import com.wat.melody.common.network.exception.IllegalIpRangesException;
import com.wat.melody.common.network.exception.IllegalPortRangesException;
import com.wat.melody.common.network.exception.IllegalProtocolsException;
import com.wat.melody.plugin.aws.ec2.common.exception.FwRuleLoaderException;
import com.wat.melody.xpathextensions.GetHeritedAttribute;

public class FwRuleLoader {

	/**
	 * The 'from' XML attribute of the 'fwrule' XML element
	 */
	public static final String FROM_ATTR = "from";

	/**
	 * The 'ports' XML attribute of the 'fwrule' XML element
	 */
	public static final String PORTS_ATTR = "ports";

	/**
	 * The 'protocols' XML attribute of the 'fwrule' XML element
	 */
	public static final String PROTOCOLS_ATTR = "protocols";

	/**
	 * The 'access' XML attribute of the 'fwrule' XML element
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

	private ITaskContext getTC() {
		return moTC;
	}

	private boolean loadFrom(Node n, FwRule fw) throws FwRuleLoaderException,
			ResourcesDescriptorException {
		String v = null;
		v = GetHeritedAttribute.getHeritedAttribute(n, FROM_ATTR);
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_EXPAND_ATTR, v, FROM_ATTR), Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setFromIpRanges(IpRanges.parseString(v));
		} catch (IllegalIpRangesException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_INVALID_ATTR, v, FROM_ATTR), Ex);
		}
		return true;
	}

	private boolean loadPorts(Node n, FwRule fw) throws FwRuleLoaderException,
			ResourcesDescriptorException {
		String v = null;
		v = GetHeritedAttribute.getHeritedAttribute(n, PORTS_ATTR);
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_EXPAND_ATTR, v, PORTS_ATTR), Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setPortRanges(PortRanges.parseString(v));
		} catch (IllegalPortRangesException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_INVALID_ATTR, v, PORTS_ATTR), Ex);
		}
		return true;
	}

	private boolean loadProtocols(Node n, FwRule fw)
			throws FwRuleLoaderException, ResourcesDescriptorException {
		String v = null;
		v = GetHeritedAttribute.getHeritedAttribute(n, PROTOCOLS_ATTR);
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_EXPAND_ATTR, v, PROTOCOLS_ATTR), Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setProtocols(Protocols.parseString(v));
		} catch (IllegalProtocolsException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_INVALID_ATTR, v, PROTOCOLS_ATTR), Ex);
		}
		return true;
	}

	private boolean loadAccess(Node n, FwRule fw) throws FwRuleLoaderException,
			ResourcesDescriptorException {
		String v = null;
		v = GetHeritedAttribute.getHeritedAttribute(n, ACCESS_ATTR);
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			v = getTC().expand(v);
		} catch (ExpressionSyntaxException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_EXPAND_ATTR, v, ACCESS_ATTR), Ex);
		}
		if (v == null || v.length() == 0) {
			return false;
		}
		try {
			fw.setAccess(Access.parseString(v));
		} catch (IllegalAccessException Ex) {
			throw new FwRuleLoaderException(Messages.bind(
					Messages.FwRuleLoadEx_INVALID_ATTR, v, ACCESS_ATTR), Ex);
		}
		return true;
	}

	/**
	 * <p>
	 * Converts selected FWRule <code>Node</code>s into {@link FwRules}. The
	 * given XPath Expression selects FWRule <code>Node</code>s.
	 * </p>
	 * 
	 * <p>
	 * <i>A FWrule <code>Node</code> must have the attributes : <BR/>
	 * * from : which should contains {@link IpRanges} ; <BR/>
	 * * to : which should contains {@link IpRanges} : <BR/>
	 * * ports : which should contains {@link PortRanges} ; <BR/>
	 * * protocols : which should contains {@link Protocols} ; <BR/>
	 * * allow : which should contains {@link Access} ; <BR/>
	 * * ref : which should contains an XPath Expression which refer to another
	 * FWRule <code>Node</code>, which attributes will be used as source ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sFWRuleXPathExpr
	 *            a String containing the XPath Expression which will be used to
	 *            found <code>Node</code>s.
	 * 
	 * @return a {@link FwRules} object, which is a collection of {@link FwRule}
	 *         .
	 * 
	 * @throws FwRuleLoaderException
	 *             if the conversion failed (ex : the XPath Expression is not
	 *             well-formed, or the content of an attribute is not valid)
	 */
	public FwRules load(NodeList nl) throws FwRuleLoaderException {
		FwRules fwrs = new FwRules();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			try {
				FwRule fw = new FwRule();
				if (!loadFrom(n, fw)) {
					continue;
				}
				loadPorts(n, fw);
				loadProtocols(n, fw);
				loadAccess(n, fw);

				fwrs.add(fw);
			} catch (FwRuleLoaderException Ex) {
				throw new FwRuleLoaderException(Messages.FwRuleLoadEx_MANAGED,
						Ex);
			} catch (ResourcesDescriptorException Ex) {
				throw new FwRuleLoaderException(Ex);
			}
		}
		return fwrs;
	}

}
