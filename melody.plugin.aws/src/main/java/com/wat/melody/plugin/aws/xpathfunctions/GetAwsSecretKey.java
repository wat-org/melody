package com.wat.melody.plugin.aws.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.plugin.aws.common.AwsPlugInConfiguration;

/**
 * <p>
 * XPath custom function, which return the AWS Secret Key (as defined in the AWS
 * Plug-In configuration file).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetAwsSecretKey implements XPathFunction {

	public static final String NAME = "getAwsSecretKey";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		try {
			return AwsPlugInConfiguration.get().getAWSSecretKey();
		} catch (PlugInConfigurationException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}