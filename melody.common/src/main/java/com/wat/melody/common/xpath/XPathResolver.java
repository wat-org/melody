package com.wat.melody.common.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathFunction;

import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xpath.exception.XPathFunctionResolverLoadingException;
import com.wat.melody.common.xpath.exception.XPathNamespaceContextResolverLoadingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathResolver {

	private XPathNamespaceContextResolver _namespaceContext;
	private XPathFunctionResolver _functionResolver;

	public XPathResolver(XPathNamespaceContextResolver namespaceResolver,
			XPathFunctionResolver functionResolver) {
		setXPathNamespaceContextResolver(namespaceResolver);
		setXPathFunctionResolver(functionResolver);
	}

	public void loadNamespaceDefinitions(PropertySet ps, String... properties)
			throws XPathNamespaceContextResolverLoadingException {
		getXPathNamespaceContextResolver().loadDefinitions(ps, properties);
	}

	public void loadNamespaceDefinition(PropertySet ps, String property)
			throws XPathNamespaceContextResolverLoadingException {
		getXPathNamespaceContextResolver().loadDefinition(ps, property);
	}

	public void addNamespaceDefinition(String namespace, String uri)
			throws XPathNamespaceContextResolverLoadingException {
		getXPathNamespaceContextResolver().addDefinition(namespace, uri);
	}

	public void loadFunctionDefinitions(PropertySet ps, String... properties)
			throws XPathFunctionResolverLoadingException {
		getXPathFunctionResolver().loadDefinitions(ps, properties);
	}

	public void loadFunctionDefinition(PropertySet ps, String property)
			throws XPathFunctionResolverLoadingException {
		getXPathFunctionResolver().loadDefinition(ps, property);
	}

	public void addFunctionDefinition(String name, String namespace,
			XPathFunction function, int funarity)
			throws XPathFunctionResolverLoadingException {
		getXPathFunctionResolver().addDefinition(name, namespace, function,
				funarity);
	}

	public XPathNamespaceContextResolver getXPathNamespaceContextResolver() {
		return _namespaceContext;
	}

	private XPathNamespaceContextResolver setXPathNamespaceContextResolver(
			XPathNamespaceContextResolver namespaceResolver) {
		if (namespaceResolver == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NamespaceContext.class.getCanonicalName() + ".");
		}
		XPathNamespaceContextResolver previous = getXPathNamespaceContextResolver();
		_namespaceContext = namespaceResolver;
		return previous;
	}

	public XPathFunctionResolver getXPathFunctionResolver() {
		return _functionResolver;
	}

	private XPathFunctionResolver setXPathFunctionResolver(
			XPathFunctionResolver functionResolver) {
		if (functionResolver == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ XPathFunctionResolver.class.getCanonicalName() + ".");
		}
		XPathFunctionResolver previous = getXPathFunctionResolver();
		_functionResolver = functionResolver;
		return previous;
	}

}