package com.wat.melody.core.internal;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalTargetFilterException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.FilterSet;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.xml.FilteredDoc;
import com.wat.melody.common.xpath.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TargetDescriptor extends FilteredDoc {

	public synchronized void load(ResourcesDescriptor doc)
			throws IllegalTargetFilterException {
		try {
			super.load(doc);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

	@Override
	public synchronized String evaluateAsString(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsString(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public synchronized NodeList evaluateAsNodeList(String expr)
			throws XPathExpressionException {
		return XPathExpander.evaluateAsNodeList(expr, getDocument()
				.getFirstChild());
	}

	@Override
	public synchronized Node evaluateAsNode(String expr)
			throws XPathExpressionException {
		return XPathExpander
				.evaluateAsNode(expr, getDocument().getFirstChild());
	}

	@Override
	public synchronized String setFilter(int i, Filter filter)
			throws IllegalTargetFilterException {
		try {
			return super.setFilter(i, filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

	@Override
	public synchronized void setFilterSet(FilterSet filters)
			throws IllegalTargetFilterException {
		try {
			super.setFilterSet(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

	@Override
	public synchronized void addFilter(Filter filter)
			throws IllegalTargetFilterException {
		try {
			super.addFilter(filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

	@Override
	public synchronized void addFilters(FilterSet filters)
			throws IllegalTargetFilterException {
		try {
			super.addFilters(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetFilterException(Ex);
		}
	}

}