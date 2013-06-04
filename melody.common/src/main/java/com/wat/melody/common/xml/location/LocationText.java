package com.wat.melody.common.xml.location;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.DocHelper;

/**
 * <p>
 * Give access to a {@link text}'s location data.
 * </p>
 * 
 * <p>
 * If the {@link text} used to build this object is not originated from a
 * {@link Doc}, this object will not be able to provide reliable informations.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocationText extends LocationAbstract implements Location {

	public LocationText(Text e) {
		super(e);
	}

	@Override
	protected Text getNode() {
		return (Text) super.getNode();
	}

	@Override
	protected Text setNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Text.class.getCanonicalName() + ".");
		}
		if (n.getNodeType() != Node.TEXT_NODE) {
			throw new IllegalArgumentException(DocHelper.parseNodeType(n)
					+ ": Not accepted. " + "Must be a valid "
					+ Text.class.getCanonicalName() + ".");
		}
		return (Text) super.setNode(n);
	}

	@Override
	public String toString() {
		return "line:" + getLine() + ", column:" + getColumn()
				+ ", text-content";
	}

	@Override
	public Element getRelatedElement() {
		return (Element) getNode().getParentNode();
	}

}