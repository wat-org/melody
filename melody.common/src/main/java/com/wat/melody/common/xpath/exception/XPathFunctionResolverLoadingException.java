package com.wat.melody.common.xpath.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathFunctionResolverLoadingException extends MelodyException {

	private static final long serialVersionUID = -2142987966432454215L;

	public XPathFunctionResolverLoadingException(String msg) {
		super(msg);
	}

	public XPathFunctionResolverLoadingException(Throwable cause) {
		super(cause);
	}

	public XPathFunctionResolverLoadingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}