package com.wat.melody.common.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;

import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.common.xpath.exception.XPathFunctionResolverLoadingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathFunctionResolver implements
		javax.xml.xpath.XPathFunctionResolver {

	private static String SUFFIX_NAMESPACE_URI = ".namespace.uri";
	private static String SUFFIX_NAME = ".name";
	private static String SUFFIX_CLASS = ".class";
	private static String SUFFIX_ARITY = ".arity";

	private static String FUNCTION_NAME_PATTERN = "[a-zA-Z]+";

	private List<XPathFunctionDefinition> _funcDefinition;

	/*
	 * TODO : externalize error messages
	 */

	public XPathFunctionResolver() {
		setXPathFunctionsDefinition(new ArrayList<XPathFunctionDefinition>());
	}

	/**
	 * <p>
	 * Load the XPathFunctions described by the given {@propertyName}'s list
	 * from the given {@link PropertiesSet}.
	 * </p>
	 * 
	 * @param ps
	 *            contains Custom XPath Function definitions to load.
	 * @param properties
	 *            indicates the properties - in the given {@link PropertiesSet}
	 *            - which contain the Custom XPath Function definitions to load.
	 * @throws XPathFunctionResolverLoadingException
	 */
	public void loadDefinitions(PropertiesSet ps, String... properties)
			throws XPathFunctionResolverLoadingException {
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
			throws XPathFunctionResolverLoadingException {
		String funcName = loadFunctionName(ps, key);
		String funcNamespace = loadFuncitonNamespace(ps, key);
		XPathFunction funcClass = loadFunctionClass(ps, key);
		int funcArity = loadFuncitonArity(ps, key);
		addDefinition(funcName, funcNamespace, funcClass, funcArity);
	}

	public void addDefinition(String name, String namespace,
			XPathFunction function, int arity) {
		getXPathFunctionsDefinition().add(
				new XPathFunctionDefinition(namespace, name, arity, function));
	}

	public String loadFunctionName(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		String functionName = null;
		if (ps.containsKey(key + SUFFIX_NAME)) {
			functionName = ps.get(key + SUFFIX_NAME);
			if (functionName.trim().length() == 0) {
				throw new XPathFunctionResolverLoadingException("key " + key
						+ SUFFIX_NAME
						+ " contains an empty xpath function name.");
			}
		} else {
			int lastIndexOfDot = key.lastIndexOf('.') + 1;
			if (lastIndexOfDot >= key.length()) {
				throw new XPathFunctionResolverLoadingException("key " + key
						+ " doesn't include an xpath function name.");
			}
			functionName = key.substring(lastIndexOfDot, key.length());
		}

		if (!functionName.matches("^" + FUNCTION_NAME_PATTERN + "$")) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ " doesn't include a valid xpath function name.");
		}
		return functionName;
	}

	public String loadFuncitonNamespace(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		if (!ps.containsKey(key + SUFFIX_NAMESPACE_URI)) {
			throw new XPathFunctionResolverLoadingException("no key " + key
					+ SUFFIX_NAMESPACE_URI);
		}
		String namespace = ps.get(key + SUFFIX_NAMESPACE_URI);
		if (namespace.trim().length() == 0) {
			throw new XPathFunctionResolverLoadingException("empty key " + key
					+ SUFFIX_NAMESPACE_URI);
		}
		return namespace;
	}

	@SuppressWarnings("unchecked")
	public XPathFunction loadFunctionClass(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		if (!ps.containsKey(key + SUFFIX_CLASS)) {
			throw new XPathFunctionResolverLoadingException("no key " + key
					+ SUFFIX_CLASS);
		}
		String classname = ps.get(key + SUFFIX_CLASS);
		if (classname.trim().length() == 0) {
			throw new XPathFunctionResolverLoadingException("empty key " + key
					+ SUFFIX_CLASS);
		}
		Class<? extends XPathFunction> c = null;
		try {
			c = (Class<? extends XPathFunction>) Class.forName(classname);
		} catch (ClassNotFoundException Ex) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_CLASS + " points to an undefined class.");
		} catch (NoClassDefFoundError Ex) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_CLASS + " points to an undefined class.");
		} catch (ClassCastException Ex) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_CLASS + " doesn't point to an XPathFunction .");
		}
		XPathFunction function = null;
		try {
			function = c.newInstance();
		} catch (InstantiationException | IllegalAccessException Ex) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_CLASS
					+ " doesn't point to a valid XPathFunction implementation.");
		}
		return function;
	}

	public int loadFuncitonArity(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		if (!ps.containsKey(key + SUFFIX_ARITY)) {
			throw new XPathFunctionResolverLoadingException("no key " + key
					+ SUFFIX_ARITY);
		}
		String sArity = ps.get(key + SUFFIX_ARITY);
		if (sArity.trim().length() == 0) {
			throw new XPathFunctionResolverLoadingException("empty key " + key
					+ SUFFIX_ARITY);
		}
		int arity = 0;
		try {
			arity = Integer.parseInt(sArity);
		} catch (NumberFormatException Ex) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_ARITY + " is not an Integer.");
		}
		if (arity < 0) {
			throw new XPathFunctionResolverLoadingException("key " + key
					+ SUFFIX_ARITY + " is not a positive Integer.");
		}
		return arity;
	}

	public List<XPathFunctionDefinition> getXPathFunctionsDefinition() {
		return _funcDefinition;
	}

	private List<XPathFunctionDefinition> setXPathFunctionsDefinition(
			List<XPathFunctionDefinition> funcsDef) {
		if (funcsDef == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<XPathFunctionDefinition>.");
		}
		List<XPathFunctionDefinition> previous = getXPathFunctionsDefinition();
		_funcDefinition = funcsDef;
		return previous;
	}

	@Override
	public XPathFunction resolveFunction(QName qname, int arity) {
		for (XPathFunctionDefinition funcDef : getXPathFunctionsDefinition()) {
			if (funcDef.matches(qname, arity)) {
				return funcDef.getFunction();
			}
		}
		return null;
	}
}
