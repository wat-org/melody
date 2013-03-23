package com.wat.melody.api.exception;

import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesDescriptorException extends NodeRelatedException {

	private static final long serialVersionUID = -2498745678654205817L;

	public ResourcesDescriptorException(Node errorNode, String msg) {
		super(errorNode, msg);
	}

	public ResourcesDescriptorException(Node errorNode, Throwable cause) {
		super(errorNode, cause);
	}

	public ResourcesDescriptorException(Node errorNode, String msg,
			Throwable cause) {
		super(errorNode, msg, cause);
	}

	@Override
	public String getErrorNodeLocationAsString() {
		return Doc.getNodeLocation(getErrorNode()).toFullString();
	}

}