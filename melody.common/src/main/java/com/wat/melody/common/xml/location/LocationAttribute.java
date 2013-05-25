package com.wat.melody.common.xml.location;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;

/**
 * <p>
 * Give access to an {@link Attr}'s location data.
 * </p>
 * 
 * <p>
 * If the {@link Attr} used to build this object is not originated from a
 * {@link Doc}, this object will not be able to provide reliable informations.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocationAttribute extends LocationAbstract implements Location {

	public LocationAttribute(Attr e) {
		super(e);
	}

	@Override
	protected Attr getNode() {
		return (Attr) super.getNode();
	}

	@Override
	protected Attr setNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Attr.class.getCanonicalName() + ".");
		}
		if (n.getNodeType() != Node.ATTRIBUTE_NODE) {
			throw new IllegalArgumentException(Doc.parseNodeType(n)
					+ ": Not accepted. " + "Must be a valid "
					+ Attr.class.getCanonicalName() + ".");
		}
		return (Attr) super.setNode(n);
	}

	@Override
	public String toString() {
		return "line:" + getLine() + ", column:" + getColumn() + ", attribute:"
				+ getNode().getName();
	}

	@Override
	public Element getRelatedElement() {
		return getNode().getOwnerElement();
	}

}