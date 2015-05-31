package com.wat.melody.core.internal.taskbuilder;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import com.wat.melody.api.ICondition;
import com.wat.melody.api.Melody;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MatchCondition implements ICondition {

	private String _expression1 = null;
	private String _expression2 = null;

	public MatchCondition(String expr1, String expr2) {
		setExpression1(expr1);
		setExpression2(expr2);
	}

	public String getExpression1() {
		return _expression1;
	}

	public String setExpression1(String expr) {
		if (expr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getExpression1();
		_expression1 = expr;
		return previous;
	}

	public String getExpression2() {
		return _expression2;
	}

	public String setExpression2(String expr) {
		if (expr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getExpression2();
		_expression2 = expr;
		return previous;
	}

	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		try {
			String expandedExpr1 = XPathExpander.expand(getExpression1(), elmt,
					ps);
			String expandedExpr2 = XPathExpander.expand(getExpression2(), elmt,
					ps);

			String expr1res = XPathExpander.expand(expandedExpr1, Melody
					.getContext().getProcessorManager()
					.getResourcesDescriptor().evaluateAsNode("/"), ps);
			String expr2res = XPathExpander.expand(expandedExpr2, Melody
					.getContext().getProcessorManager()
					.getResourcesDescriptor().evaluateAsNode("/"), ps);
			return expr1res.equals(expr2res);
		} catch (XPathExpressionException | ExpressionSyntaxException ignored) {
			return false;
		}
	}

}