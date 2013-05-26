package com.wat.melody.common.xml.exception;

import java.util.LinkedHashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NodeRelatedException extends MelodyException {

	private static final long serialVersionUID = -8978423233111135477L;

	private Set<Node> _errorNodes = new LinkedHashSet<Node>();

	public NodeRelatedException(Node errorNode, String msg) {
		super(msg);
		addErrorNode(errorNode);
	}

	public NodeRelatedException(Node errorNode, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		addErrorNode(errorNode);
	}

	public NodeRelatedException(Node errorNode, String msg, Throwable cause) {
		super(msg, cause);
		addErrorNode(errorNode);
	}

	public NodeRelatedException(Set<Node> errorNodes, String msg) {
		super(msg);
		setErrorNodes(errorNodes);
	}

	public NodeRelatedException(Set<Node> errorNodes, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		setErrorNodes(errorNodes);
	}

	public NodeRelatedException(Set<Node> errorNodes, String msg,
			Throwable cause) {
		super(msg, cause);
		setErrorNodes(errorNodes);
	}

	public NodeRelatedException(NodeList errorNodes, String msg) {
		super(msg);
		setErrorNodes(errorNodes);
	}

	public NodeRelatedException(NodeList errorNodes, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		setErrorNodes(errorNodes);
	}

	public NodeRelatedException(NodeList errorNodes, String msg, Throwable cause) {
		super(msg, cause);
		setErrorNodes(errorNodes);
	}

	public Set<Node> getErrorNodes() {
		return _errorNodes;
	}

	private void setErrorNodes(Set<Node> errorNodes) {
		if (errorNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ Node.class.getCanonicalName() + ">.");
		}
		if (errorNodes.size() == 0) {
			throw new IllegalArgumentException("empty: Not accepted. "
					+ "Must at least contains one "
					+ Node.class.getCanonicalName() + ".");
		}
		_errorNodes = errorNodes;
	}

	private void setErrorNodes(NodeList errorNodes) {
		if (errorNodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ Node.class.getCanonicalName() + ">.");
		}
		Set<Node> nodes = new LinkedHashSet<Node>();
		for (int i = 0; i < errorNodes.getLength(); i++) {
			nodes.add(errorNodes.item(i));
		}
		setErrorNodes(nodes);
	}

	public void addErrorNode(Node errorNode) {
		if (errorNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		getErrorNodes().add(errorNode);
	}

	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		for (Node errorNode : getErrorNodes()) {
			str.append("[");
			str.append(getErrorNodeLocationAsString(errorNode));
			str.append("] ");
		}
		str.append(super.getMessage());
		return str.toString();
	}

	public String getErrorNodeLocationAsString(Node errorNode) {
		return Doc.getNodeLocation(errorNode).toFullString();
	}

}