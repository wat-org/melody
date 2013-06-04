package com.wat.melody.common.xml.location;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.wat.melody.common.xml.DocHelper;

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
	 *             if the given {@link Node} is neither an {@link Element}, nor
	 *             an {@link Attr}, nor an {@link Text}.
	 */
	public static Location newLocation(Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			return new LocationElement((Element) n);
		} else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
			return new LocationAttribute((Attr) n);
		} else if (n.getNodeType() == Node.TEXT_NODE) {
			return new LocationText((Text) n);
		}
		throw new IllegalArgumentException(
				LocationFactory.class.getCanonicalName() + " doesn't support '"
						+ DocHelper.parseNodeType(n) + "' yet (only accept ["
						+ DocHelper.parseNodeType(Node.ELEMENT_NODE) + ", "
						+ DocHelper.parseNodeType(Node.ATTRIBUTE_NODE) + ", "
						+ DocHelper.parseNodeType(Node.TEXT_NODE) + "]).");
	}

}