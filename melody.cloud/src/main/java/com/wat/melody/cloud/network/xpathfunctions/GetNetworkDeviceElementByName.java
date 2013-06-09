package com.wat.melody.cloud.network.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.cloud.network.NetworkDevicesHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetNetworkDeviceElementByName implements XPathFunction {

	public static final String NAME = "getNetworkDeviceElementByName";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		/*
		 * arg1 can be String or Attr. Sample :
		 * 
		 * getNetworkDeviceElementByName(//srv[1],'eth0')
		 * getNetworkDeviceElementByName(//srv[1],//n[4]/@device-name-ref)
		 */
		// will not fail: have been registered with arity 2
		Object arg0 = list.get(0);
		Object arg1 = list.get(1);
		try {
			if (XPathFunctionHelper.isElement(arg0)) {
				if (arg1 instanceof String) {
					return NetworkDevicesHelper.findNetworkDeviceElementByName(
							(Element) arg0, (String) arg1);
				} else if (arg1 instanceof Attr) {
					return NetworkDevicesHelper.findNetworkDeviceElementByName(
							(Element) arg0, ((Attr) arg1).getValue());
				}
			}
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects an "
					+ Element.class.getCanonicalName() + " as first "
					+ "argument and a " + String.class.getCanonicalName()
					+ " or an " + Attr.class.getCanonicalName()
					+ " as second argument.");
		} catch (NodeRelatedException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}