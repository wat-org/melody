package com.wat.melody.common.xml.exception;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

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
		super(getNodeInfo(errorNode) + ": Invalid content.", cause);
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
		String msg = super.getMessage();
		return "[" + getErrorNodeLocationAsString() + "] "
				+ (msg != null ? msg : "");
	}

	public String getErrorNodeLocationAsString() {
		return DocHelper.getNodeLocation(getErrorNode()).toFullString();
	}

	private static String getNodeInfo(Node errorNode) {
		if (errorNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		if (errorNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			return "value '" + ((Attr) errorNode).getValue() + "'";
		}
		if (errorNode.getNodeType() == Node.ELEMENT_NODE) {
			return "element '" + ((Element) errorNode).getNodeName() + "'";
		}
		if (errorNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			return "text '" + ((Text) errorNode).getTextContent() + "'";
		}
		throw new IllegalArgumentException(errorNode.getNodeType()
				+ ": Not accepted. "
				+ "Must be either an Att, an Element, or a Text node.");
	}

}