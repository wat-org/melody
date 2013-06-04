package com.wat.melody.common.xml.location;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.common.xml.Parser;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LocationAbstract implements Location {

	private Node _node;

	public LocationAbstract(Node node) {
		setNode(node);
	}

	protected Node getNode() {
		return _node;
	}

	protected Node setNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		Node previous = getNode();
		_node = n;
		return previous;
	}

	@Override
	public String toFullString() {
		return "file:" + getSource() + ", " + toString();
	}

	@Override
	public String getSource() {
		Element n = getRelatedElement();
		String source = null;
		do {
			source = (String) n.getUserData(Parser.SOURCE);
			if (source != null) {
				return source;
			}
		} while ((n = (Element) n.getParentNode()) != null);
		return null;
	}

	@Override
	public Integer getLine() {
		Object line = getRelatedElement().getUserData(Parser.LINE_NUMBER);
		return (line == null) ? null : (Integer) line;
	}

	@Override
	public Integer getColumn() {
		Object col = getRelatedElement().getUserData(Parser.COLUMN_NUMBER);
		return (col == null) ? null : (Integer) col;
	}

	public abstract Element getRelatedElement();
}
