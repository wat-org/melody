package com.wat.melody.cloud.protectedarea.xml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.instance.xml.InstanceDatasHelper;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.protectedarea.Messages;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.ProtectedAreaNames;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ProtectedAreaHelper {

	/**
	 * XML Element, which contains Protected Area Management datas, related to
	 * an Instance Element (more formally called the
	 * "Protected Area Management Element").
	 */
	public static final String PROTECTED_AREA_MGMT_ELEMENT = "protected-area-management";

	/**
	 * XPath Expression which select the Protected Area Management Element,
	 * related to an Instance Element.
	 */
	public static final String PROTECTED_AREA_MGMT_ELEMENT_SELECTOR = "//"
			+ PROTECTED_AREA_MGMT_ELEMENT;

	/**
	 * XML attribute of the Protected Area Management Element, which contains
	 * the beginning of the regular expression which targets Protected Area
	 * Elements.
	 */
	public static final String PROTECTED_AREA_SELECTOR = "protected-areas-selector";

	/**
	 * Default value of the regular expression which targets Protected Area
	 * Elements.
	 */
	public static final String DEFAULT_PROTECTED_AREA_SELECTOR = ".//protected-areas//protected-area";

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance.
	 * 
	 * @return the Protected Area Management Element related to the given
	 *         Instance, which is :
	 *         <ul>
	 *         <li>The last Protected Area Management Element related to the
	 *         given Instance, if multiple Protected Area Management Elements
	 *         are found ;</li>
	 *         <li><tt>null</tt>, if no Protected Area Management Element are
	 *         found ;</li>
	 *         </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 */
	public static Element findProtectedAreaManagementElement(
			Element instanceElmt) {
		NodeList nl = null;
		try {
			nl = FilteredDocHelper.getHeritedContent(instanceElmt,
					PROTECTED_AREA_MGMT_ELEMENT_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '"
					+ PROTECTED_AREA_MGMT_ELEMENT_SELECTOR + "'. "
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
	 *            is an {@link Element} which describes a Protected Area
	 *            Management Element related to an Instance. Can be
	 *            <tt>null</tt>, if the related Instance has no Protected Area
	 *            Management Element.
	 * 
	 * @return the Protected Area Selector, which is :
	 *         <ul>
	 *         <li>The Default Protected Area Selector, if the given Protected
	 *         Area Management Element is <tt>null</tt> ;</li>
	 *         <li>The Default Protected Area Selector, if the given Protected
	 *         Area Management Element is not <tt>null</tt> but has no Custom
	 *         Protected Area Selector is defined in ;</li>
	 *         <li>The Custom Protected Area Selector defined in the given
	 *         Protected Area Management Element ;</li>
	 *         </ul>
	 */
	public static String getProtectedAreaSelector(Element mgmtElmt) {
		try {
			return mgmtElmt.getAttributeNode(PROTECTED_AREA_SELECTOR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_PROTECTED_AREA_SELECTOR;
		}
	}

	public static ProtectedAreaIds findInstanceProtectedAreaIds(
			Element instanceElmt) throws NodeRelatedException {
		ProtectedAreaNames names = InstanceDatasHelper
				.findInstanceProtectedAreaNames(instanceElmt);
		// Must convert each Protected Area Name to its Identifier
		return convertProtectedAreaFromNamesToIds(instanceElmt,
				FilteredDocHelper.getHeritedAttribute(instanceElmt,
						InstanceDatasLoader.PROTECTED_AREAS_ATTR), names);
	}

	/**
	 * @param instanceElmt
	 *            is an {@link Element} which describes an Instance, where are
	 *            defined the given Protected Area Names.
	 * @param paattr
	 *            is the XML Attribute which contains the Protected Area Names
	 *            to convert (useful for error message).
	 * @param names
	 *            are the Protected Area Names to convert to their Protected
	 *            Area Identifiers. Can be <tt>null</tt>.
	 * 
	 * @return the {@link ProtectedAreaIds}, corresponding to the given
	 *         {@link ProtectedAreaNames}. Can be an empty set, when the given
	 *         names is <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion fails (no id specified, invalid id
	 *             specified).
	 */
	public static ProtectedAreaIds convertProtectedAreaFromNamesToIds(
			Element instanceElmt, Attr paattr, ProtectedAreaNames names)
			throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		ProtectedAreaIds ids = new ProtectedAreaIds();
		if (names == null || names.size() == 0) {
			return ids;
		}
		Element mgmtElmt = ProtectedAreaHelper
				.findProtectedAreaManagementElement(instanceElmt);
		String selector = ProtectedAreaHelper
				.getProtectedAreaSelector(mgmtElmt);
		for (ProtectedAreaName name : names) {
			String exp = selector + "[@" + ProtectedAreaDatasLoader.NAME_ATTR
					+ "='" + name.getValue() + "']";
			Node n = null;
			try {
				n = XPathExpander.evaluateAsNode(exp,
						instanceElmt.getOwnerDocument());
			} catch (XPathExpressionException Ex) {
				throw new NodeRelatedException(mgmtElmt, Msg.bind(
						Messages.ProtectedAreaEx_SELECTOR_NOT_XPATH, exp), Ex);
			}
			if (n == null) {
				throw new NodeRelatedException(paattr, Msg.bind(
						Messages.ProtectedAreaEx_NOT_DEFINED, name));
			}
			Attr a = ((Element) n)
					.getAttributeNode(ProtectedAreaDatasLoader.ID_ATTR);
			if (a == null) {
				throw new NodeRelatedException(paattr, Msg.bind(
						Messages.ProtectedAreaEx_ID_NOT_DEFINED, name));
			}
			String res = a.getNodeValue();
			if (res == null || res.trim().length() == 0) {
				throw new NodeRelatedException(paattr, Msg.bind(
						Messages.ProtectedAreaEx_ID_EMPTY, name));
			}
			ProtectedAreaId id = null;
			try {
				id = ProtectedAreaId.parseString(res);
			} catch (IllegalProtectedAreaIdException Ex) {
				throw new NodeRelatedException(paattr, Msg.bind(
						Messages.ProtectedAreaEx_ID_INVALID, name, res), Ex);
			}
			ids.add(id);
		}
		return ids;
	}

}