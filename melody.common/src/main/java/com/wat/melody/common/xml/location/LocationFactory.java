package com.wat.melody.common.xml.location;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;

/**
 * <p>
 * Build {@link Location} object, related to the given {@link Node}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocationFactory {

	/**
	 * <p>
	 * Build {@link Location} object, related to the given {@link Node}.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Node} is not an {@link Element} or an
	 *             {@link Attr}.
	 */
	public static Location newLocation(Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			return new LocationElement((Element) n);
		} else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
			return new LocationAttribute((Attr) n);
		}
		throw new IllegalArgumentException(
				LocationFactory.class.getCanonicalName() + " doesn't support '"
						+ Doc.parseNodeType(n) + "' yet (only accept ["
						+ Doc.parseNodeType(Node.ELEMENT_NODE) + ", "
						+ Doc.parseNodeType(Node.ATTRIBUTE_NODE) + "]).");
	}

}