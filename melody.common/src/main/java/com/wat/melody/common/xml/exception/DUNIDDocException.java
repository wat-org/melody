package com.wat.melody.common.xml.exception;

import org.w3c.dom.Node;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DUNIDDocException extends NodeRelatedException {

	private static final long serialVersionUID = -3479406546216984215L;

	public DUNIDDocException(Node errorNode, String msg) {
		super(errorNode, msg);
	}

	public DUNIDDocException(Node errorNode, Throwable cause) {
		super(errorNode, cause);
	}

	public DUNIDDocException(Node errorNode, String msg, Throwable cause) {
		super(errorNode, msg, cause);
	}

}