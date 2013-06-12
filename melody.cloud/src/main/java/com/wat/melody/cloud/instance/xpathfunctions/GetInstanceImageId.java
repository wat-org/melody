package com.wat.melody.cloud.instance.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;

import com.wat.melody.cloud.instance.xml.InstanceDatasHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetInstanceImageId implements XPathFunction {

	public static final String NAME = "getInstanceImageId";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object evaluate(List list) throws XPathFunctionException {
		// will not fail: have been registered with arity 1
		Object arg0 = list.get(0);
		try {
			if (XPathFunctionHelper.isElement(arg0)) {
				return InstanceDatasHelper.findInstanceImageId((Element) arg0);
			} else if (XPathFunctionHelper.isElementList(arg0)) {
				return InstanceDatasHelper
						.findInstanceImageId((List<Element>) arg0);
			}
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects an "
					+ Element.class.getCanonicalName() + " or a "
					+ List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + "> as first "
					+ "argument.");
		} catch (NodeRelatedException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}