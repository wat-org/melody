package com.wat.melody.cloud.network.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.xml.FilteredDocHelper;
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
	public static final String NETWORK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE = "network-devices-selector";

	/**
	 * Default XPath Expression to select Network Devices Elements, related to
	 * an Instance Element.
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_ELEMENTS_SELECTOR = "//"
			+ NetworkDevicesLoader.DEFAULT_NETWORK_DEVICE_ELEMENT;

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Network Management Element related to the given Instance,
	 *         which is :
	 *         <ul>
	 *         <li>The last Network Management Element related to the given
	 *         Instance, if Network Management Elements are found ;</li>
	 *         <li><tt>null</tt>, if no Network Management Element are found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 */
	public static Element findNetworkManagementElement(Element instanceElmt) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt,
					NETWORK_MGMT_ELEMENT_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '"
					+ NETWORK_MGMT_ELEMENT_SELECTOR + "'. "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() == 0) {
			return null;
		}
		// Conversion can't fail: the expression can only return Element
		return (Element) nl.item(nl.getLength() - 1);
	}

	/**
	 * @param mgmtElmt
	 *            is an {@link Element} which describes a Network Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Network Management Element.
	 * 
	 * @return the Network Devices Selector, which is :
	 *         <ul>
	 *         <li>the Default Network Devices Selector, if the given Network
	 *         Management Element is <tt>null</tt> ;</li>
	 *         <li>the Default Network Devices Selector, if the given Network
	 *         Management Element is not <tt>null</tt> but has no Custom Network
	 *         Devices Selector is defined in ;</li>
	 *         <li>The Custom Network Devices Selector defined in the given
	 *         Network Management Element ;</li>
	 *         </ul>
	 */
	public static String getNetworkDeviceElementsSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(
					NETWORK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_DEVICE_ELEMENTS_SELECTOR;
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
		Element mgmtElmt = findNetworkManagementElement(instanceElmt);
		return getNetworkDeviceElementByName(instanceElmt, mgmtElmt, devname);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to the given Instance. Can be <tt>null</tt>,
	 *            if the given Instance has no Network Management Element.
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
	 *             given Instance's Network Management Element) is not a valid
	 *             XPath Expression ;</li>
	 *             <li>if the Custom Network Devices Selector (found in the
	 *             given Instance's Network Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static Element getNetworkDeviceElementByName(Element instanceElmt,
			Element mgmtElmt, String devname) throws NodeRelatedException {
		// Called by an XPath Function, so devname must be a string
		try {
			NetworkDeviceName.parseString(devname);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new IllegalArgumentException(Ex);
		}
		String selector = "." + getNetworkDeviceElementsSelector(mgmtElmt)
				+ "[@" + NetworkDevicesLoader.DEVICE_NAME_ATTR + "='" + devname
				+ "']";
		try {
			// Conversion can't fail: the expression can only return Element
			return (Element) XPathExpander.evaluateAsNode(selector,
					instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(NETWORK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Messages.bind(Messages.NetMgmtEx_SELECTOR_INVALID_XPATH,
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
		Element mgmtElmt = findNetworkManagementElement(instanceElmt);
		return getNetworkDeviceElements(instanceElmt, mgmtElmt);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * @param mgmtElmt
	 *            is an {@link Element} which describes the Network Management
	 *            Element related to the given Instance. Can be <tt>null</tt>,
	 *            if the given Instance has no Network Management Element.
	 * 
	 * @return all Instance's Network Device {@link Element}s. Can be an empty
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
	public static List<Element> getNetworkDeviceElements(Element instanceElmt,
			Element mgmtElmt) throws NodeRelatedException {
		String selector = "." + getNetworkDeviceElementsSelector(mgmtElmt);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList(selector, instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(NETWORK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Messages.bind(Messages.NetMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(NETWORK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Messages.bind(Messages.NetMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

}