package com.wat.melody.core.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

/**
 * <p>
 * XPath custom function, which throws an exception.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RaiseError implements XPathFunction {

	public static final String NAME = "raiseError";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		// will not fail: have been registered with arity 1
		throw new XPathFunctionException(String.valueOf(list.get(0)));
	}

}