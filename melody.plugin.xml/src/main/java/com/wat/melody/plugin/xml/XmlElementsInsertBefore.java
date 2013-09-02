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
@Task(name = XmlElementsInsertBefore.INSERT_XML_ELEMENTS)
public class XmlElementsInsertBefore extends BaseAddXmlElements implements
		ITask {

	private static Logger log = LoggerFactory
			.getLogger(XmlElementsInsertBefore.class);

	/**
	 * Task's name
	 */
	public static final String INSERT_XML_ELEMENTS = "insert-xml-elements-before";

	public XmlElementsInsertBefore() {
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

		Doc doctoinsert = getContentToAddAsDoc();
		String res = "";
		for (Element pos : getSelectedElements()) {
			for (int i = 0; i < doctoinsert.getDocument().getFirstChild()
					.getChildNodes().getLength(); i++) {
				Node imported = getPreparedDoc().getDocument().importNode(
						doctoinsert.getDocument().getFirstChild()
								.getChildNodes().item(i), true);
				pos.getParentNode().insertBefore(imported, pos);
			}
			res += "\n  " + DocHelper.getXPathPosition((Element) pos);
		}

		log.debug(getExtraLogInfo() + " Insert:" + "\n"
				+ getContentToAddAsString() + "\n"
				+ "before XML Element Nodes:" + res);

		savePreparedDoc();
	}

}