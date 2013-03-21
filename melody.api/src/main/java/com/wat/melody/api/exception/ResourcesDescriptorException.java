package com.wat.melody.api.exception;

import org.w3c.dom.Node;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesDescriptorException extends MelodyException {

	private static final long serialVersionUID = -2498745678654205817L;

	private Node moErrorNode = null;

	public ResourcesDescriptorException(Node errorNode, String msg) {
		super(msg);
		setErrorNode(errorNode);
	}

	public ResourcesDescriptorException(Node errorNode, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		setErrorNode(errorNode);
	}

	public ResourcesDescriptorException(Node errorNode, String msg,
			Throwable cause) {
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

	@Override
	public String getMessage() {
		return "[" + Doc.getNodeLocation(getErrorNode()).toFullString() + "] "
				+ super.getMessage();
	}

}