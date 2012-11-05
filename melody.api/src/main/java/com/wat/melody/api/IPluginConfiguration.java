package com.wat.melody.api;

import com.wat.melody.api.exception.PluginConfigurationException;
import com.wat.melody.common.utils.PropertiesSet;

public interface IPluginConfiguration {

	public static final String PLUGIN_CONF_CLASS = "plugin.configuration.canonicalclassname";

	public static final String PLUGIN_CONF_NAME = "plugin.configuration.name";

	public String getName();

	public void load(PropertiesSet ps) throws PluginConfigurationException;

	public String getFilePath();

}
