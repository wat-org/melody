package com.wat.melody.api;

import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.utils.PropertiesSet;

public interface IPlugInConfiguration {

	public static final String PLUGIN_CONF_CLASS = "plugin.configuration.canonicalclassname";

	public void load(PropertiesSet ps) throws PlugInConfigurationException;

	public String getFilePath();

}
