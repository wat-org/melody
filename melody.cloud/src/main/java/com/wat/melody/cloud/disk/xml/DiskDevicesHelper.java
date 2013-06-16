package com.wat.melody.cloud.disk.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.Messages;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DiskDevicesHelper {

	/**
	 * XML Element, which contains Disk Management datas, related to an Instance
	 * Element (more formally called the "Disk Management Element").
	 */
	public static final String DISK_MGMT_ELEMENT = "disk-management";

	/**
	 * XPath Expression which select the Disk Management Element, related to an
	 * Instance Element.
	 */
	public static final String DISK_MGMT_ELEMENT_SELECTOR = "//"
			+ DISK_MGMT_ELEMENT;

	/**
	 * XML attribute of the Disk Management Element, which contains the XPath
	 * Expression to select Disk Devices Elements.
	 */
	public static final String DISK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE = "disk-devices-selector";

	/**
	 * Default XPath Expression to select Disk Device Elements, related to an
	 * Instance Element.
	 */
	public static final String DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR = "//"
			+ DiskDevicesLoader.DEFAULT_DISK_DEVICE_ELEMENT;

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Disk Management Element related to the given Instance, which
	 *         is :
	 *         <ul>
	 *         <li>The last Disk Management Element related to the given
	 *         Instance, if Disk Management Elements are found ;</li>
	 *         <li><tt>null</tt>, if no Disk Management Element are found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 */
	public static Element findDiskManagementElement(Element instanceElmt) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt,
					DISK_MGMT_ELEMENT_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '" + DISK_MGMT_ELEMENT_SELECTOR
					+ "'. " + "Because this XPath Expression is hard coded, "
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
	 *            is an {@link Element} which describes a Disk Management
	 *            Element related to an Instance. Can be <tt>null</tt>, if the
	 *            related Instance has no Disk Management Element.
	 * 
	 * @return the Disk Devices Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR}, if the given
	 *         Disk Management Element is <tt>null</tt> ;</li>
	 *         <li>{@link #DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR}, if the given
	 *         Disk Management Element is not <tt>null</tt> but has no Custom
	 *         Disk Devices Selector is defined in ;</li>
	 *         <li>The Custom Disk Devices Selector defined in the given Disk
	 *         Management Element ;</li>
	 *         </ul>
	 */
	public static String getDiskDeviceElementsSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(
					DISK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE).getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR;
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return all Disk Device {@link Element}s of the given Instance. Can be an
	 *         empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom Disk Devices Selector (found in the given
	 *             Instance's Disk Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if the Custom Disk Devices Selector (found in the given
	 *             Instance's Disk Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findDiskDevices(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = findDiskManagementElement(instanceElmt);
		String selector = getDiskDeviceElementsSelector(mgmtElmt);
		NodeList nl;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt, selector);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(DISK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.DiskMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					mgmtElmt.getAttributeNode(DISK_DEVICE_ELEMENTS_SELECTOR_ATTRIBUTE),
					Msg.bind(Messages.DiskMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

}