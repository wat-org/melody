package com.wat.melody.api;

import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IPlugInConfiguration {

	public static final String PLUGIN_CONF_CLASS = "plugin.configuration.canonicalclassname";

	public void load(PropertySet ps) throws PlugInConfigurationException;

	public String getFilePath();

}
