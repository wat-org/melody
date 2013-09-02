package com.wat.melody.plugin.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.plugin.xml.common.BaseAddXmlElements;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = XmlElementsAppend.APPEND_XML_ELEMENTS)
public class XmlElementsAppend extends BaseAddXmlElements implements ITask {

	private static Logger log = LoggerFactory
			.getLogger(XmlElementsAppend.class);

	/**
	 * Task's name
	 */
	public static final String APPEND_XML_ELEMENTS = "append-xml-elements";

	public XmlElementsAppend() {
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

		Doc doctoappend = getContentToAddAsDoc();
		String res = "";
		for (Element pos : getSelectedElements()) {
			for (int i = 0; i < doctoappend.getDocument().getFirstChild()
					.getChildNodes().getLength(); i++) {
				Node imported = getPreparedDoc().getDocument().importNode(
						doctoappend.getDocument().getFirstChild()
								.getChildNodes().item(i), true);
				pos.appendChild(imported);
			}
			res += "\n  " + DocHelper.getXPathPosition(pos);
		}

		log.debug(getExtraLogInfo() + " Append:" + "\n"
				+ getContentToAddAsString() + "\n" + "to XML Element Nodes:"
				+ res);

		savePreparedDoc();
	}

}