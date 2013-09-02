package com.wat.melody.core.nativeplugin.sequence.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;

import com.wat.melody.api.Melody;

/**
 * <p>
 * XPath custom function, which return the current sequence descriptor's path.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetSequenceDescriptorPath implements XPathFunction {

	public static final String NAME = "getSequenceDescriptorPath";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) {
		return Melody.getContext().getProcessorManager()
				.getSequenceDescriptor().getSourceFile();
	}

}