package com.wat.melody.common.xml.location;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;

/**
 * <p>
 * Give access to an {@link Element}'s location data.
 * </p>
 * 
 * <p>
 * If the {@link Element} used to build this object is not originated from a
 * {@link Doc}, this object will not be able to provide reliable informations.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocationElement extends LocationAbstract implements Location {

	public LocationElement(Element e) {
		super(e);
	}

	@Override
	protected Element getNode() {
		return (Element) super.getNode();
	}

	@Override
	protected Element setNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new IllegalArgumentException(Doc.parseNodeType(n)
					+ ": Not accepted. " + "Must be a valid "
					+ Element.class.getCanonicalName() + ".");
		}
		return (Element) super.setNode(n);
	}

	@Override
	public String toString() {
		return "line:" + getLine() + ", column:" + getColumn();
	}

	@Override
	public Element getRelatedElement() {
		return getNode();
	}

}