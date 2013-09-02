package com.wat.melody.plugin.xml;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.plugin.xml.common.BaseXmlElements;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;
import com.wat.melody.plugin.xml.common.types.Attribute;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = XmlAttributesRemove.REMOVE_XML_ATTRIBUTES)
public class XmlAttributesRemove extends BaseXmlElements implements ITask {

	private static Logger log = LoggerFactory
			.getLogger(XmlAttributesRemove.class);

	/**
	 * Task's name
	 */
	public static final String REMOVE_XML_ATTRIBUTES = "remove-xml-attributes";

	public static final String ATTRIBUTE_NE = "attribute";

	private List<Attribute> _attributes = new ArrayList<Attribute>();

	public XmlAttributesRemove() {
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
		for (Element e : getSelectedElements()) {
			for (Attribute p : getAttributes()) {
				e.removeAttribute(p.getAttributeName());
			}
			res += "\n  " + DocHelper.getXPathPosition(e);
		}

		log.debug(getExtraLogInfo() + " Remove attributes: " + "\n  "
				+ getAttributes() + "\n" + "from XML Element Nodes:" + res);

		savePreparedDoc();
	}

	public List<Attribute> getAttributes() {
		return _attributes;
	}

	@NestedElement(name = ATTRIBUTE_NE, mandatory = true)
	public void addAttribute(Attribute attribute) {
		if (attribute == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Attribute.class.getCanonicalName()
					+ ".");
		}
		_attributes.add(attribute);
	}

}