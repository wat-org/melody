package com.wat.melody.common.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;

import com.wat.melody.common.properties.PropertiesSet;
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

	public XPathFunctionResolver() {
		setXPathFunctionsDefinition(new ArrayList<XPathFunctionDefinition>());
	}

	/**
	 * <p>
	 * Load the Custom XPath Function definitions described by the given
	 * properties's list from the given {@link PropertiesSet}.
	 * </p>
	 * 
	 * @param ps
	 *            contains Custom XPath Function definitions to load.
	 * @param properties
	 *            indicates the properties - in the given {@link PropertiesSet}
	 *            - which contain the Custom XPath Function definitions to load.
	 * 
	 * @throws XPathFunctionResolverLoadingException
	 *             if a Custom XPath Function definition is invalid.
	 */
	public void loadDefinitions(PropertiesSet ps, String... properties)
			throws XPathFunctionResolverLoadingException {
		if (ps == null) {
			return;
		}
		if (properties == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid list of properties's name.");
		}

		for (String property : properties) {
			if (property == null) {
				continue;
			}
			property = property.trim();
			if (property.length() == 0) {
				continue;
			}
			loadDefinition(ps, property);
		}
	}

	public void loadDefinition(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		try {
			String funcName = loadFunctionName(ps, key);
			String funcNamespace = loadFunctionNamespace(ps, key);
			XPathFunction funcClass = loadFunctionClass(ps, key);
			int funcArity = loadFunctionArity(ps, key);
			addDefinition(funcName, funcNamespace, funcClass, funcArity);
		} catch (XPathFunctionResolverLoadingException Ex) {
			throw new XPathFunctionResolverLoadingException(Messages.bind(
					Messages.XPathResolver_INVALID_FUNCTION_DEFINITION, key),
					Ex);
		}
	}

	public void addDefinition(String name, String namespace,
			XPathFunction function, int arity) {
		getXPathFunctionsDefinition().add(
				new XPathFunctionDefinition(namespace, name, arity, function));
	}

	private String loadFunctionName(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		String functionName = null;
		if (ps.containsKey(key + SUFFIX_NAME)) {
			functionName = ps.get(key + SUFFIX_NAME);
			try {
				validateFunctionName(functionName);
			} catch (XPathFunctionResolverLoadingException Ex) {
				throw new XPathFunctionResolverLoadingException(
						Messages.bind(
								Messages.XPathResolver_INVALID_FUNCTION_NAME_DEFINITION,
								key + SUFFIX_NAME), Ex);
			}
		} else {
			int lastIndexOfDot = key.lastIndexOf('.') + 1;
			functionName = key.substring(lastIndexOfDot, key.length());
			validateFunctionName(functionName);
		}

		return functionName;
	}

	private void validateFunctionName(String functionName)
			throws XPathFunctionResolverLoadingException {
		if (functionName.trim().length() == 0
				|| !functionName.matches("^" + FUNCTION_NAME_PATTERN + "$")) {
			throw new XPathFunctionResolverLoadingException(Messages.bind(
					Messages.XPathResolver_INVALID_FUNCTION_NAME, functionName,
					FUNCTION_NAME_PATTERN));
		}
	}

	private String loadFunctionNamespace(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		try {
			if (!ps.containsKey(key + SUFFIX_NAMESPACE_URI)) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_MISSING_FUNCTION_NAMESPACE_URI,
						key + SUFFIX_NAMESPACE_URI));
			}
			String namespace = ps.get(key + SUFFIX_NAMESPACE_URI);
			if (namespace.trim().length() == 0) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_NAMESPACE_URI,
						namespace));
			}
			return namespace;
		} catch (XPathFunctionResolverLoadingException Ex) {
			throw new XPathFunctionResolverLoadingException(
					Messages.bind(
							Messages.XPathResolver_INVALID_FUNCTION_NAMESPACE_URI_DEFINITION,
							key + SUFFIX_NAMESPACE_URI), Ex);
		}
	}

	@SuppressWarnings("unchecked")
	private XPathFunction loadFunctionClass(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		try {
			if (!ps.containsKey(key + SUFFIX_CLASS)) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_MISSING_FUNCTION_CLASS, key
								+ SUFFIX_CLASS));
			}
			String classname = ps.get(key + SUFFIX_CLASS);
			if (classname.trim().length() == 0) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_CLASS_EMPTY,
						classname));
			}
			Class<? extends XPathFunction> c = null;
			try {
				c = (Class<? extends XPathFunction>) Class.forName(classname);
			} catch (ClassNotFoundException Ex) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_CLASS_CNF,
						classname));
			} catch (NoClassDefFoundError Ex) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_CLASS_NCDF,
						classname, Ex.getMessage().replaceAll("/", ".")));
			}
			XPathFunction function = null;
			try {
				function = c.newInstance();
			} catch (InstantiationException | IllegalAccessException Ex) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_CLASS_IA,
						classname));
			} catch (ClassCastException Ex) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_CLASS_CC,
						classname, XPathFunction.class.getCanonicalName()));
			}
			return function;
		} catch (XPathFunctionResolverLoadingException Ex) {
			throw new XPathFunctionResolverLoadingException(Messages.bind(
					Messages.XPathResolver_INVALID_FUNCTION_CLASS_DEFINITION,
					key + SUFFIX_CLASS), Ex);
		}
	}

	private int loadFunctionArity(PropertiesSet ps, String key)
			throws XPathFunctionResolverLoadingException {
		try {
			if (!ps.containsKey(key + SUFFIX_ARITY)) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_MISSING_FUNCTION_ARITY, key
								+ SUFFIX_ARITY));
			}
			String sArity = ps.get(key + SUFFIX_ARITY);
			if (sArity.trim().length() == 0) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_ARITY, sArity));
			}
			int arity = 0;
			try {
				arity = Integer.parseInt(sArity);
			} catch (NumberFormatException Ex) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_ARITY, sArity));
			}
			if (arity < 0) {
				throw new XPathFunctionResolverLoadingException(Messages.bind(
						Messages.XPathResolver_INVALID_FUNCTION_ARITY, sArity));
			}
			return arity;
		} catch (XPathFunctionResolverLoadingException Ex) {
			throw new XPathFunctionResolverLoadingException(Messages.bind(
					Messages.XPathResolver_INVALID_FUNCTION_ARITY_DEFINITION,
					key + SUFFIX_ARITY), Ex);
		}
	}

	private List<XPathFunctionDefinition> getXPathFunctionsDefinition() {
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
