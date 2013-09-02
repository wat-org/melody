package com.wat.melody.core.nativeplugin.sequence.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;

import com.wat.melody.api.Melody;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * <p>
 * XPath custom function, which return the sequence descriptor's basedir.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetBaseDir implements XPathFunction {

	public static final String NAME = "getBaseDir";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) {
		return XPathFunctionHelper.toString(Melody.getContext()
				.getProcessorManager().getSequenceDescriptor().getBaseDir());
	}

}