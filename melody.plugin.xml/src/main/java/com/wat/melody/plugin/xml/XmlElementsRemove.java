package com.wat.melody.plugin.xml;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.plugin.xml.common.BaseXmlElements;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = XmlElementsRemove.REMOVE_XML_ELEMENTS)
public class XmlElementsRemove extends BaseXmlElements implements ITask {

	private static Logger log = LoggerFactory
			.getLogger(XmlElementsRemove.class);

	/**
	 * Task's name
	 */
	public static final String REMOVE_XML_ELEMENTS = "remove-xml-elements";

	public XmlElementsRemove() {
		super();
	}

	@Override
	public void validate() throws XmlPluginException {
		super.validate();
	}

	@Override
	public void doProcessing() throws XmlPluginException {
		if (!shouldProcess()) {
			return;
		}
		super.doProcessing();

		String res = "";
		for (Element pos : getSelectedElements()) {
			Node removed = pos.getParentNode().removeChild(pos);
			Doc dump = new Doc();
			try {
				dump.loadFromXML("<s/>");
			} catch (MelodyException | IOException ignored) {
				throw new RuntimeException("shouldn't happened.");
			}
			Node imported = dump.getDocument().importNode(removed, true);
			dump.getDocument().getFirstChild().appendChild(imported);
			String part = dump.dump();
			part = part.substring(part.indexOf("\n") + 1,
					part.lastIndexOf("\n"));
			res += "\n" + part;
		}

		log.debug(getExtraLogInfo() + " Removed XML Element Nodes:" + res);

		savePreparedDoc();
	}

}