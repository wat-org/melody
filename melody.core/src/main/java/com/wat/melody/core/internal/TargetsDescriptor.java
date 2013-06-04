package com.wat.melody.core.internal;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.IllegalTargetsFilterException;
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
public class TargetsDescriptor extends FilteredDoc {

	public synchronized void load(ResourcesDescriptor doc)
			throws IllegalTargetsFilterException {
		try {
			super.load(doc);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetsFilterException(Ex);
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
	protected String getSmartMsg() {
		return "targets-descriptor  ";
	}

	@Override
	public synchronized Filter setFilter(int i, Filter filter)
			throws IllegalTargetsFilterException {
		try {
			return super.setFilter(i, filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetsFilterException(Ex);
		}
	}

	@Override
	public synchronized void setFilterSet(FilterSet filters)
			throws IllegalTargetsFilterException {
		try {
			super.setFilterSet(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetsFilterException(Ex);
		}
	}

	@Override
	public synchronized void addFilter(Filter filter)
			throws IllegalTargetsFilterException {
		try {
			super.addFilter(filter);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetsFilterException(Ex);
		}
	}

	@Override
	public synchronized void addFilters(FilterSet filters)
			throws IllegalTargetsFilterException {
		try {
			super.addFilters(filters);
		} catch (IllegalFilterException Ex) {
			throw new IllegalTargetsFilterException(Ex);
		}
	}

}