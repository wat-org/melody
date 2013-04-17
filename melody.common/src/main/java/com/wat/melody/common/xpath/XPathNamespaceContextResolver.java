package com.wat.melody.common.xpath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.common.xpath.exception.XPathNamespaceContextResolverLoadingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathNamespaceContextResolver implements NamespaceContext {

	private static String SUFFIX_NAMESPACE_URI = ".uri";

	private static String NAMESPACE_NAME_PATTERN = "[a-zA-Z]+";

	/**
	 * Keys is a 'namespace' / value is its corresponding 'namespace's uri'
	 */
	private Map<String, String> _nsDefintion;

	public XPathNamespaceContextResolver() {
		setNamespaceDefinition(new HashMap<String, String>());
	}

	/**
	 * <p>
	 * Load the NameSpaces described by the given {@propertyName}'s list from
	 * the given {@link PropertiesSet}.
	 * </p>
	 * 
	 * @param ps
	 *            contains Custom XPath Function definitions to load.
	 * @param properties
	 *            indicates the properties - in the given {@link PropertiesSet}
	 *            - which contain the Custom NameSpace definitions to load.
	 * @throws XPathNamespaceContextResolverLoadingException
	 */
	public void loadDefinitions(PropertiesSet ps, String... properties)
			throws XPathNamespaceContextResolverLoadingException {
		if (ps == null) {
			return;
		}
		if (properties == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid list of "
					+ PropertyName.class.getCanonicalName() + ".");
		}

		for (String property : properties) {
			property = property.trim();
			if (property.length() == 0) {
				continue;
			}
			loadDefinition(ps, property);
		}
	}

	public void loadDefinition(PropertiesSet ps, String key)
			throws XPathNamespaceContextResolverLoadingException {
		try {
			String namespace = loadNamespace(ps, key);
			String uri = loadNamespaceURI(ps, key);
			addDefinition(namespace, uri);
		} catch (XPathNamespaceContextResolverLoadingException Ex) {
			throw new XPathNamespaceContextResolverLoadingException(
					Messages.bind(
							Messages.XPathResolver_INVALID_NAMESPACE_DEFINITION,
							key), Ex);
		}
	}

	public void addDefinition(String name, String uri) {
		getNamespaceDefinition().put(name, uri);
	}

	public String loadNamespace(PropertiesSet ps, String key)
			throws XPathNamespaceContextResolverLoadingException {
		int lastIndexOfDot = key.lastIndexOf('.') + 1;
		if (lastIndexOfDot >= key.length()) {
			throw new XPathNamespaceContextResolverLoadingException(
					Messages.bind(
							Messages.XPathResolver_INVALID_NAMESPACE_NAME, "",
							NAMESPACE_NAME_PATTERN));
		}
		String namespaceName = key.substring(lastIndexOfDot, key.length());
		if (!namespaceName.matches("^" + NAMESPACE_NAME_PATTERN + "$")) {
			throw new XPathNamespaceContextResolverLoadingException(
					Messages.bind(
							Messages.XPathResolver_INVALID_NAMESPACE_NAME,
							namespaceName, NAMESPACE_NAME_PATTERN));
		}
		return namespaceName;
	}

	public String loadNamespaceURI(PropertiesSet ps, String key)
			throws XPathNamespaceContextResolverLoadingException {
		try {
			if (!ps.containsKey(key + SUFFIX_NAMESPACE_URI)) {
				throw new XPathNamespaceContextResolverLoadingException(
						Messages.bind(
								Messages.XPathResolver_MISSING_NAMESPACE_URI,
								key + SUFFIX_NAMESPACE_URI));
			}
			String namespace = ps.get(key + SUFFIX_NAMESPACE_URI);
			if (namespace.trim().length() == 0) {
				throw new XPathNamespaceContextResolverLoadingException(
						Messages.bind(
								Messages.XPathResolver_INVALID_NAMESPACE_URI,
								namespace));
			}
			return namespace;
		} catch (XPathNamespaceContextResolverLoadingException Ex) {
			throw new XPathNamespaceContextResolverLoadingException(
					Messages.bind(
							Messages.XPathResolver_INVALID_NAMESPACE_URI_DEFINITION,
							key + SUFFIX_NAMESPACE_URI), Ex);
		}
	}

	public Map<String, String> getNamespaceDefinition() {
		return _nsDefintion;
	}

	private Map<String, String> setNamespaceDefinition(Map<String, String> nsDef) {
		if (nsDef == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Map<String, String>.");
		}
		Map<String, String> previous = getNamespaceDefinition();
		_nsDefintion = nsDef;
		return previous;
	}

	@Override
	public String getNamespaceURI(String nsSearched) {
		for (String ns : getNamespaceDefinition().keySet()) {
			if (nsSearched.equals(ns)) {
				return getNamespaceDefinition().get(ns);
			}
		}
		return null;
	}

	@Override
	public String getPrefix(String s) {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String s) {
		return null;
	}

}
