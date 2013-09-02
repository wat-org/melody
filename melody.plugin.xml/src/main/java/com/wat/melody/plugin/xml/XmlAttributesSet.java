package com.wat.melody.plugin.xml;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.plugin.xml.common.BaseXmlElements;
import com.wat.melody.plugin.xml.common.exception.XmlPluginException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = XmlAttributesSet.SET_XML_ATTRIBUTES)
public class XmlAttributesSet extends BaseXmlElements implements ITask {

	private static Logger log = LoggerFactory.getLogger(XmlAttributesSet.class);

	/**
	 * Task's name
	 */
	public static final String SET_XML_ATTRIBUTES = "set-xml-attributes";

	public static final String ATTRIBUTE_NE = "attribute";

	private List<Property> _attributes = new ArrayList<Property>();

	public XmlAttributesSet() {
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
			for (Property p : getAttributes()) {
				e.setAttribute(p.getName().getValue(), p.getValue());
			}
			res += "\n  " + DocHelper.getXPathPosition(e);
		}

		log.debug(getExtraLogInfo() + " Set attributes: " + "\n  "
				+ getAttributes() + "\n" + "to XML Element Nodes:" + res);

		savePreparedDoc();
	}

	public List<Property> getAttributes() {
		return _attributes;
	}

	@NestedElement(name = ATTRIBUTE_NE, mandatory = true)
	public void addAttribute(Property attribute) {
		if (attribute == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Property.class.getCanonicalName()
					+ ".");
		}
		_attributes.add(attribute);
	}

}