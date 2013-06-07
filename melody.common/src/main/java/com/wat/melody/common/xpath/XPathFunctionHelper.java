package com.wat.melody.common.xpath;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class XPathFunctionHelper {

	/**
	 * @param anObject
	 *            is an object to test.
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
	 *            is an object to test.
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
	 * @param anObject
	 *            is an object to convert to a <tt>String</tt>.
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
	 * @param anObject
	 *            is a {@link List} of objects to convert to a {@link List} of
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