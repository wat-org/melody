package com.wat.melody.plugin.xml.common;

import java.io.IOException;

import org.w3c.dom.Element;

import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;
import com.wat.melody.plugin.xml.common.types.NodeContent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class BaseAddXmlElements extends BaseXmlElements {

	public static final String NODE_CONTENT_NE = "node-content";

	private Doc _toAdd = null;

	public BaseAddXmlElements() {
		super();
	}

	protected Doc getContentToAddAsDoc() {
		return _toAdd;
	}

	protected String getContentToAddAsString() {
		if (_toAdd == null) {
			return null;
		}
		String part = _toAdd.dump();
		return part.substring(part.indexOf("\n") + 1, part.lastIndexOf("\n"));
	}

	@NestedElement(name = NODE_CONTENT_NE, type = Type.ADD)
	public void addNodeContent(NodeContent nodeContent)
			throws XmlPluginException {
		if (nodeContent == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + NodeContent.class.getCanonicalName()
					+ ".");
		}

		// if already define => raise an error
		if (_toAdd != null) {
			throw new XmlPluginException(Msg.bind(
					Messages.NodeContentEx_DUPLICATE_DECLARATION,
					NODE_CONTENT_NE));
		}

		// nodeContent must be valid XML syntax
		Doc toadd = new Doc();
		try {
			toadd.loadFromXML("<a>" + nodeContent.getNodeContent() + "</a>");
		} catch (MelodyException | IOException Ex) {
			throw new XmlPluginException(Messages.NodeContentEx_NOT_XML, Ex);
		}

		// remove useless Text Nodes
		DocHelper.removeTextNode((Element) toadd.getDocument().getFirstChild());

		_toAdd = toadd;
	}

}