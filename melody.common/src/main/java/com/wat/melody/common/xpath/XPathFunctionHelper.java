package com.wat.melody.common.xpath;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathFunctionHelper {

	/**
	 * @param anObject
	 *            is the object to test.
	 * 
	 * @return <tt>true</tt> if the given object is an {@link Element}, or
	 *         <tt>false</tt> if the given object is not an {@link Element} or
	 *         <tt>null</tt>.
	 */
	public static boolean isElement(Object anObject) {
		if (anObject == null) {
			return false;
		} else if (anObject instanceof Element) {
			return true;
		}
		return false;
	}

	/**
	 * @param anObject
	 *            is the object to test.
	 * 
	 * @return <tt>true</tt> if the given object is a {@link List} of
	 *         {@link Element}, or <tt>false</tt> if the given object is not a
	 *         {@link List} of {@link Element} or <tt>null</tt>.
	 */
	public static boolean isElementList(Object anObject) {
		if (anObject == null) {
			return false;
		} else if (anObject instanceof List) {
			for (Object item : (List<?>) anObject) {
				if (!isElement(item)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param nodelist
	 *            is the object to convert.
	 * 
	 * @return a {@link List} of {@link Element}, or <tt>null</tt> if the given
	 *         {@link NodeList} is <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link NodeList} doesn't contains only
	 *             {@link Element}.
	 */
	public static List<Element> toElementList(NodeList nodeList) {
		if (nodeList == null) {
			return null;
		}
		List<Element> list = new ArrayList<Element>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (!isElement(nodeList.item(i))) {
				throw new IllegalArgumentException(nodeList.item(i).getClass()
						.getCanonicalName()
						+ ": Not accepted. Cannot be converted to "
						+ Element.class.getCanonicalName() + ".");
			}
			list.add((Element) nodeList.item(i));
		}
		return list;
	}

	/**
	 * <p>
	 * This method differs from {@link String#valueOf()} in the way that it will
	 * return <tt>null</tt> if the given object is <tt>null</tt> (when
	 * {@link String#valueOf()} will return the <tt>String</tt> 'null').
	 * </p>
	 * 
	 * @param anObject
	 *            is the object to convert to a <tt>String</tt>.
	 * 
	 * @return a <tt>String</tt> if the given object is not <tt>null</tt>, or
	 *         <tt>null</tt> if the given object is <tt>null</tt>.
	 */
	public static String toString(Object anObject) {
		if (anObject == null) {
			return null;
		}
		return String.valueOf(anObject);
	}

	/**
	 * <p>
	 * This method differs from {@link String#valueOf()} in the way that it will
	 * return <tt>null</tt> if the given object is <tt>null</tt> (when
	 * {@link String#valueOf()} will return the <tt>String</tt> 'null').
	 * </p>
	 * 
	 * @param anObject
	 *            is the {@link List} of objects to convert to a {@link List} of
	 *            <tt>String</tt>s.
	 * 
	 * @return a {@link List} of <tt>String</tt> or <tt>null</tt> elements, if
	 *         the given object is not <tt>null</tt>, or <tt>null</tt> if the
	 *         given object is <tt>null</tt>.
	 */
	public static List<String> toString(List<?> anObject) {
		if (anObject == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		for (Object item : (ArrayList<?>) anObject) {
			list.add(toString(item));
		}
		return list;
	}

}