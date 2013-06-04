package com.wat.melody.common.xml.exception;

import org.w3c.dom.Node;

import com.wat.melody.common.xml.DocHelper;

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

	@Override
	public String getErrorNodeLocationAsString() {
		return DocHelper.getNodeLocation(getErrorNode()).toString();
	}

}