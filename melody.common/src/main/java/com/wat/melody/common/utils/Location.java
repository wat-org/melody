package com.wat.melody.common.utils;

import java.io.File;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Location {

	private Node moNode;

	/**
	 * 
	 * <p>
	 * <i>
	 * <ul>
	 * <li>The given {@link Node} must be originated from a {@link Doc} ;</li>
	 * <li>Can be a {@link Node} or an {@link Attr} ;</li>
	 * </ul>
	 * </i>
	 * </p>
	 * 
	 * @param node
	 */
	public Location(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		moNode = node;
	}

	public File getFile() {
		return (File) moNode.getOwnerDocument().getUserData(Parser.FILE);
	}

	public int getLine() {
		Node n = moNode;
		if (moNode instanceof Attr) {
			n = ((Attr) moNode).getOwnerElement();
		}
		return (int) n.getUserData(Parser.LINE_NUMBER);
	}

	public int getColumn() {
		Node n = moNode;
		if (moNode instanceof Attr) {
			n = ((Attr) moNode).getOwnerElement();
		}
		return (int) n.getUserData(Parser.COLUMN_NUMBER);
	}

	@Override
	public String toString() {
		return "line:"
				+ getLine()
				+ ", column:"
				+ getColumn()
				+ (moNode instanceof Attr ? ", attribute:"
						+ moNode.getNodeName() : "");
	}

	public String toFullString() {
		return "file:" + getFile() + ", " + toString();
	}

}
