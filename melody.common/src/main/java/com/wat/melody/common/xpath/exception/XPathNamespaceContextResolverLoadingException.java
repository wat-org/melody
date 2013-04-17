package com.wat.melody.common.xpath.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathNamespaceContextResolverLoadingException extends
		MelodyException {

	private static final long serialVersionUID = -2314321324233653202L;

	public XPathNamespaceContextResolverLoadingException() {
		super();
	}

	public XPathNamespaceContextResolverLoadingException(String msg) {
		super(msg);
	}

	public XPathNamespaceContextResolverLoadingException(Throwable cause) {
		super(cause);
	}

	public XPathNamespaceContextResolverLoadingException(String msg,
			Throwable cause) {
		super(msg, cause);
	}

}