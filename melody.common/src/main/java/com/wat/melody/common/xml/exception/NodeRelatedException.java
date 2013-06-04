package com.wat.melody.common.xml.exception;

import org.w3c.dom.Node;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.DocHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NodeRelatedException extends MelodyException {

	private static final long serialVersionUID = -8978423233111135477L;

	private Node _errorNode;

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
		return _errorNode;
	}

	private void setErrorNode(Node errorNode) {
		if (errorNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		_errorNode = errorNode;
	}

	@Override
	public String getMessage() {
		return "[" + getErrorNodeLocationAsString() + "] " + super.getMessage();
	}

	public String getErrorNodeLocationAsString() {
		return DocHelper.getNodeLocation(getErrorNode()).toFullString();
	}

}