package com.wat.melody.plugin.xml.common.types;

import com.wat.melody.api.annotation.TextContent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NodeContent {

	private String _nodeContent = null;

	public String getNodeContent() {
		return _nodeContent;
	}

	@TextContent(mandatory = true)
	public String setNodeContent(String nodeContent) {
		String previous = getNodeContent();
		_nodeContent = nodeContent;
		return previous;
	}

}