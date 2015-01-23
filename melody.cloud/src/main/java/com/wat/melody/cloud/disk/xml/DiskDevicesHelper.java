package com.wat.melody.cloud.disk.xml;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.Messages;
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
	public static final String DISK_DEVICE_ELEMENTS_SELECTOR = "disk-devices-selector";

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
	 * @return the Disk Devices Selector, which is :
	 *         <ul>
	 *         <li>{@link #DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR}, if the given
	 *         element has no Disk Management Element ;</li>
	 *         <li>{@link #DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR}, if the given
	 *         element has a Disk Management Element which has no Custom Disk
	 *         Devices Selector defined in ;</li>
	 *         <li>The Custom Disk Devices Selector defined in the given
	 *         element's Disk Management Element ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	public static String getDiskDeviceElementsSelector(Element instanceElmt) {
		return getDiskDeviceElementsSelectorAttr(instanceElmt).getValue();
	}

	private static Attr getDiskDeviceElementsSelectorAttr(Element instanceElmt) {
		try {
			return DocHelper.getAttribute(instanceElmt, "./"
					+ DISK_MGMT_ELEMENT + "/@" + DISK_DEVICE_ELEMENTS_SELECTOR,
					DEFAULT_DISK_DEVICE_ELEMENTS_SELECTOR);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return all Disk Device {@link Element}s of the given element. Can be an
	 *         empty list.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             <ul>
	 *             <li>if the Custom Disk Devices Selector (found in the given
	 *             element's Disk Management Element) is not a valid XPath
	 *             Expression ;</li>
	 *             <li>if the Custom Disk Devices Selector (found in the given
	 *             element's Disk Management Element) doesn't select
	 *             {@link Element}s ;</li>
	 *             </ul>
	 */
	public static List<Element> findDiskDevices(Element instanceElmt)
			throws NodeRelatedException {
		String selector = getDiskDeviceElementsSelector(instanceElmt);
		NodeList nl;
		try {
			nl = XPathExpander.evaluateAsNodeList("." + selector, instanceElmt);
		} catch (XPathExpressionException Ex) {
			throw new NodeRelatedException(
					getDiskDeviceElementsSelectorAttr(instanceElmt), Msg.bind(
							Messages.DiskMgmtEx_SELECTOR_INVALID_XPATH,
							selector), Ex);
		}
		try {
			return XPathFunctionHelper.toElementList(nl);
		} catch (IllegalArgumentException Ex) {
			throw new NodeRelatedException(
					getDiskDeviceElementsSelectorAttr(instanceElmt), Msg.bind(
							Messages.DiskMgmtEx_SELECTOR_NOT_MATCH_ELMT,
							selector));
		}
	}

}