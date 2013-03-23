package com.wat.melody.common.xml.exception;

import org.w3c.dom.Node;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class FilteredDocException extends NodeRelatedException {

	private static final long serialVersionUID = -8787423432309890095L;

	public FilteredDocException(Node errorNode, String msg) {
		super(errorNode, msg);
	}

	public FilteredDocException(Node errorNode, Throwable cause) {
		super(errorNode, cause);
	}

	public FilteredDocException(Node errorNode, String msg, Throwable cause) {
		super(errorNode, msg, cause);
	}

}