package com.wat.melody.core.internal;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalTargetFilterException;
import com.wat.melody.common.utils.FilteredDoc;
import com.wat.melody.common.utils.exception.IllegalFilterException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TargetDescriptor extends FilteredDoc {

	public void load(ResourcesDescriptor doc) throws IllegalTargetFilterException {
		try {
			super.load(doc);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

	@Override
	public synchronized String evaluateAsString(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsString(sXPathExpr, getDocument().getFirstChild());
	}

	@Override
	public synchronized NodeList evaluateAsNodeList(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNodeList(sXPathExpr, getDocument().getFirstChild());
	}

	@Override
	public synchronized Node evaluateAsNode(String sXPathExpr)
			throws XPathExpressionException {
		return evaluateAsNode(sXPathExpr, getDocument().getFirstChild());
	}

}
