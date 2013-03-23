package com.wat.melody.common.xml.exception;

import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NodeRelatedException extends IllegalDocException {

	private static final long serialVersionUID = -8978423233111135477L;

	private Node moErrorNode = null;

	public NodeRelatedException(Node errorNode, String msg) {
		super(msg);
		setErrorNode(errorNode);
	}

	public NodeRelatedException(Node errorNode, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		setErrorNode(errorNode);
	}

	public NodeRelatedException(Node errorNode, String msg, Throwable cause) {
		super(msg, cause);
		setErrorNode(errorNode);
	}

	public Node getErrorNode() {
		return moErrorNode;
	}

	private void setErrorNode(Node errorNode) {
		if (errorNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		moErrorNode = errorNode;
	}

	public String getErrorNodeLocationAsString() {
		return Doc.getNodeLocation(getErrorNode()).toFullString();
	}

	@Override
	public String getMessage() {
		return "[" + getErrorNodeLocationAsString() + "] " + super.getMessage();
	}

}