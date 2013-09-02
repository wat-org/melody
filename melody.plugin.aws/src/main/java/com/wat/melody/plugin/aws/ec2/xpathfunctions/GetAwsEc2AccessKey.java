package com.wat.melody.plugin.aws.ec2.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.plugin.aws.ec2.common.AwsPlugInConfiguration;

/**
 * <p>
 * XPath custom function, which return the AWS EC2 Access Key (as defined in the
 * AWS EC2 Plug-In configuration file).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class GetAwsEc2AccessKey implements XPathFunction {

	public static final String NAME = "getAwsEc2AccessKey";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		try {
			return AwsPlugInConfiguration.get().getAWSAccessKeyId();
		} catch (PlugInConfigurationException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}