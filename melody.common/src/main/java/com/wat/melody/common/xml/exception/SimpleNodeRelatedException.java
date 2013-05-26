package com.wat.melody.common.xml.exception;

import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleNodeRelatedException extends NodeRelatedException {

	private static final long serialVersionUID = -8978423233111135477L;

	public SimpleNodeRelatedException(Node errorNode, String msg) {
		super(errorNode, msg);
	}

	public SimpleNodeRelatedException(Node errorNode, Throwable cause) {
		super(errorNode, cause);
	}

	public SimpleNodeRelatedException(Node errorNode, String msg,
			Throwable cause) {
		super(errorNode, msg, cause);
	}

	public SimpleNodeRelatedException(Set<Node> errorNodes, String msg) {
		super(errorNodes, msg);
	}

	public SimpleNodeRelatedException(Set<Node> errorNodes, Throwable cause) {
		super(errorNodes, cause);
	}

	public SimpleNodeRelatedException(Set<Node> errorNodes, String msg,
			Throwable cause) {
		super(errorNodes, msg, cause);
	}

	public SimpleNodeRelatedException(NodeList errorNodes, String msg) {
		super(errorNodes, msg);
	}

	public SimpleNodeRelatedException(NodeList errorNodes, Throwable cause) {
		super(errorNodes, cause);
	}

	public SimpleNodeRelatedException(NodeList errorNodes, String msg,
			Throwable cause) {
		super(errorNodes, msg, cause);
	}

	@Override
	public String getErrorNodeLocationAsString(Node errorNode) {
		return Doc.getNodeLocation(errorNode).toString();
	}

}