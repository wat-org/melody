package com.wat.melody.core.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;

import com.wat.melody.api.Melody;

/**
 * <p>
 * XPath custom function, which return the working folder path as a
 * <tt>String</tt>.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetWorkingFolder implements XPathFunction {

	public static final String NAME = "getWorkingFolder";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) {
		return Melody.getContext().getProcessorManager().getWorkingFolderPath();
	}

}