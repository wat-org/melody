package com.wat.melody.cloud.protectedarea.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.instance.xml.InstanceDatasHelper;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.protectedarea.Messages;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.ProtectedAreaNames;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.xpathextensions.XPathHelper;

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
			Element instanceElmt, String region) throws NodeRelatedException {
		ProtectedAreaNames names = InstanceDatasHelper
				.findInstanceProtectedAreaNames(instanceElmt);
		// Must convert each Protected Area Name to its Identifier
		try {
			return convertProtectedAreaFromNamesToIds(instanceElmt, names,
					region);
		} catch (Exception Ex) {
			throw new NodeRelatedException(
					FilteredDocHelper.getHeritedAttribute(instanceElmt,
							InstanceDatasLoader.PROTECTED_AREAS_ATTR),
					"Failed to get Protected Area Identifiers from Protected "
							+ "Area Names.", Ex);
		}
	}

	/**
	 * @param elmt
	 *            is an {@link Element} which describes an Instance or a
	 *            protected area, where are defined the given Protected Area
	 *            Names.
	 * @param names
	 *            are the Protected Area Names to convert to their Protected
	 *            Area Identifiers. Can be <tt>null</tt>.
	 * 
	 * @return the {@link ProtectedAreaIds}, corresponding to the given
	 *         {@link ProtectedAreaNames}. Can be an empty set, when the given
	 *         names is <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 * @throws Exception
	 *             if the conversion fails (no id specified, invalid id
	 *             specified).
	 */
	public static ProtectedAreaIds convertProtectedAreaFromNamesToIds(
			Element elmt, ProtectedAreaNames names, String region)
			throws Exception {
		if (elmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		ProtectedAreaIds ids = new ProtectedAreaIds();
		if (names == null || names.size() == 0) {
			return ids;
		}
		Element mgmtElmt = ProtectedAreaHelper
				.findProtectedAreaManagementElement(elmt);
		String selector = ProtectedAreaHelper
				.getProtectedAreaSelector(mgmtElmt);
		for (ProtectedAreaName name : names) {
			String exp = selector + "[@" + ProtectedAreaDatasLoader.NAME_ATTR
					+ "='" + name.getValue() + "']";
			/*
			 * the xpath expr can return multiple protected areas, located in
			 * different regions
			 */
			NodeList nl = null;
			try {
				nl = XPathExpander.evaluateAsNodeList(exp,
						elmt.getOwnerDocument());
			} catch (XPathExpressionException Ex) {
				throw new NodeRelatedException(mgmtElmt, Msg.bind(
						Messages.ProtectedAreaEx_SELECTOR_NOT_XPATH, exp), Ex);
			}
			if (nl == null || nl.getLength() == 0) {
				throw new Exception(Msg.bind(
						Messages.ProtectedAreaEx_NOT_DEFINED, name, region));
			}
			// Handle the region
			Element paNode = null;
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				String nRegion = XPathHelper.getHeritedAttributeValue(e,
						ProtectedAreaDatasLoader.REGION_ATTR);
				if (region.equals(nRegion)) {
					// the correct protected area have been found
					// (e.g. name and region match)
					if (paNode != null) {
						throw new Exception(Msg.bind(
								Messages.ProtectedAreaEx_MULTIPLE_DEFINITION,
								name,
								region,
								"\nhere ["
										+ DocHelper.getNodeLocation(e)
												.toFullString() + "]",
								"\nand here ["
										+ DocHelper.getNodeLocation(paNode)
												.toFullString() + "]"));
					}
					paNode = e;
				}
			}
			if (paNode == null) {
				throw new Exception(Msg.bind(
						Messages.ProtectedAreaEx_NOT_DEFINED, name, region));
			}
			// get the protected area id attribute
			Attr a = paNode.getAttributeNode(ProtectedAreaDatasLoader.ID_ATTR);
			if (a == null) {
				throw new Exception(Msg.bind(
						Messages.ProtectedAreaEx_ID_NOT_DEFINED, name, region));
			}
			String res = a.getNodeValue();
			if (res == null || res.trim().length() == 0) {
				throw new Exception(Msg.bind(Messages.ProtectedAreaEx_ID_EMPTY,
						name, region));
			}
			// convert the protected area id attribute to a protected area id
			ProtectedAreaId id = null;
			try {
				id = ProtectedAreaId.parseString(res);
			} catch (IllegalProtectedAreaIdException Ex) {
				throw new Exception(
						Msg.bind(Messages.ProtectedAreaEx_ID_INVALID, name,
								region, res), Ex);
			}
			ids.add(id);
		}
		return ids;
	}

	public static List<String> findProtectedAreaId(List<Element> paElmts)
			throws NodeRelatedException {
		if (paElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<String> list = new ArrayList<String>();
		for (Element instanceElmt : paElmts) {
			if (instanceElmt == null) {
				continue;
			}
			String res = findProtectedAreaId(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static String findProtectedAreaId(Element paElmt)
			throws NodeRelatedException {
		if (paElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		// 'id' attr cannot be herited and cannot contains Melody XPath Expr
		String v = paElmt.getAttribute(ProtectedAreaDatasLoader.ID_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	public static List<ProtectedAreaName> findProtectedAreaName(
			List<Element> paElmts) throws NodeRelatedException {
		if (paElmts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid a " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		List<ProtectedAreaName> list = new ArrayList<ProtectedAreaName>();
		for (Element instanceElmt : paElmts) {
			if (instanceElmt == null) {
				continue;
			}
			ProtectedAreaName res = findProtectedAreaName(instanceElmt);
			if (res != null) {
				list.add(res);
			}
		}
		return list;
	}

	public static ProtectedAreaName findProtectedAreaName(Element paElmt)
			throws NodeRelatedException {
		if (paElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be valid an " + Element.class.getCanonicalName()
					+ ".");
		}
		// 'name' attr cannot be herited and cannot contains Melody XPath Expr
		String v = paElmt.getAttribute(ProtectedAreaDatasLoader.NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return ProtectedAreaName.parseString(v);
		} catch (IllegalProtectedAreaNameException Ex) {
			Attr attr = paElmt
					.getAttributeNode(ProtectedAreaDatasLoader.NAME_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	public static List<String> findProtectedAreaRegion(List<Element> paElmts)
			throws NodeRelatedException {
		return InstanceDatasHelper.findInstanceRegion(paElmts);
	}

	public static String findProtectedAreaRegion(Element paElmt)
			throws NodeRelatedException {
		return InstanceDatasHelper.findInstanceRegion(paElmt);
	}

}