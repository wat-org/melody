package com.wat.melody.common.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathFunctionDefinition {

	private QName _qname;
	private int _arity;
	private XPathFunction _function;

	public XPathFunctionDefinition(String namespace, String name, int arity,
			XPathFunction function) {
		setQName(namespace, name);
		setArity(arity);
		setFunction(function);
	}

	public boolean matches(QName qname, int arity) {
		return getQName().equals(qname) && getArity() == arity;
	}

	public QName getQName() {
		return _qname;
	}

	private QName setQName(String namespace, String name) {
		if (namespace == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		QName previous = getQName();
		_qname = new QName(namespace, name);
		return previous;
	}

	public int getArity() {
		return _arity;
	}

	private int setArity(int arity) {
		if (arity < 0) {
			throw new IllegalArgumentException(arity + ": Not accepted. "
					+ "Must be a positive Integer.");
		}
		int previous = getArity();
		_arity = arity;
		return previous;
	}

	public XPathFunction getFunction() {
		return _function;
	}

	private XPathFunction setFunction(XPathFunction function) {
		if (function == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ XPathFunction.class.getCanonicalName() + ".");
		}
		XPathFunction previous = getFunction();
		_function = function;
		return previous;
	}

}