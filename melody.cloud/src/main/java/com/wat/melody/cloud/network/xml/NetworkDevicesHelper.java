package com.wat.melody.cloud.network.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkDevicesHelper {

	/**
	 * XML Element, which contains Network Management datas, related to an
	 * Instance Element (more formally called the "Network Management Element").
	 */
	public static final String NETWORK_MGMT_ELEMENT = "network-management";

	/**
	 * XPath Expression which select the Network Management Element, related to
	 * an Instance Element.
	 */
	public static final String NETWORK_MGMT_ELEMENT_SELECTOR = "//"
			+ NETWORK_MGMT_ELEMENT;

	/**
	 * XML attribute of the Network Management Element, which contains the XPath
	 * Expression to select Network Devices Elements.
	 */
	public static final String NETWORK_DEVICE_ELEMENTS_SELECTOR = "network-devices-selector";

	/**
	 * Default XPath Expression to select Network Devices Elements, related to
	 * an Instance Element.
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_ELEMENTS_SELECTOR = "//"
			+ NetworkDevicesLoader.DEFAULT_NETWORK_DEVICE_ELEMENT;

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Network Management Element of the given element, or
	 *         <tt>null</tt> if the given element has no Network Management
	 *         Element defined in.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static Element findNetworkManagementElement(Element e) {
		try {
			return (Element) XPathExpander.evaluateAsNode("."
					+ NetworkDevicesHelper.NETWORK_MGMT_ELEMENT_SELECTOR, e);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while "
					+ "retrieving the Network Management Element of "
					+ "an Instance. "
					+ "Because this expression is hard-coded, such "
					+ "error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.");
		}
	}

	/**
	 * @param e
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Network Devices Selector, which is :
	 *         <ul>
	 *         <li>the Default Network Devices Selector, if the given element
	 *         has no Network Management Element ;</li>
	 *         <li>the Default Network Devices Selector, if the given element
	 *         has a Network Management Element which has no Custom Network
	 *         Devices Selector defined in ;</li>
	 *         <li>The Custom Network Devices Selector defined in the given
	 *         elment's Network Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getNetworkDeviceElementsSelector(Element e) {
		return getNetworkDeviceElementsSelectorAttr(e).getValue();
	}

	private static Attr getNetworkDeviceElementsSelectorAttr(Element e) {
		try {
			return DocHelper.getAttribute(e, "./" + NETWORK_MGMT_ELEMENT + "/@"
					+ NETWORK_DEVICE_ELEMENTS_SELECTOR,
					DEFAULT_NETOWRK_DEVICE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param devname
	 *            is the name of the Network Device {@link Element} to retrieve.
	 * 
	 * @return The Instance's Network Device {@link Element}, whose match the
	 *         given name, or <tt>null</tt>, it no Network Device
	 *         {@link Element} match the given name.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given Instance is <tt>null</tt> ;</li>
	 *             <li>if the given name is <tt>null</tt> ;</li>
	 *             <li>if the given name is not a valid Network Device Name ;</li>
	 *             </ul>
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom Network Devices Selector (found in the
	 *             Instance's Network Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if the Custom Network Devices Selector (found in the
	 *             Instance's Network Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static Element findNetworkDeviceElementByName(Element instanceElmt,
			String devname) throws NodeRelatedException {
		// Called by an XPath Function, so devname must be a string
		try {
			NetworkDeviceName.parseString(devname);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new IllegalArgumentException(Ex);
		}
		String selector = "." + getNetworkDeviceElementsSelector(instanceElmt)
				+ "[@" + NetworkDevicesLoader.DEVICE_NAME_ATTR + "='" + devname
				+ "']";
		try {
			// Conversion can't fail: the expression can only return Element
			return (Element) XPathExpander.evaluateAsNode(selector,
					instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getNetworkDeviceElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.NetMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return All Instance's Network Device {@link Element}s. Can be an empty
	 *         list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom Network Devices Selector (found in the
	 *             given Instance's Network Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom Network Devices Selector (found in the
	 *             given Instance's Network Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findNetworkDeviceElements(Element instanceElmt)
			throws NodeRelatedException {
		String selector = "." + getNetworkDeviceElementsSelector(instanceElmt);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList(selector, instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getNetworkDeviceElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.NetMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getNetworkDeviceElementsSelectorAttr(instanceElmt),
					Msg.bind(Messages.NetMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

}