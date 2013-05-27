package com.wat.melody.core.nativeplugin.setEDAttrValue;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.core.nativeplugin.setEDAttrValue.exception.SetAttributeValueException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SetAttributeValue implements ITask {

	/**
	 * The 'SetAttributeValue' XML element used in the Sequence Descriptor
	 */
	public static final String UPDATE_ED_ATTR = "SetAttributeValue";

	/**
	 * The 'target' XML attribute of the 'SetAttributeValue' XML element
	 */
	public static final String TARGET_ATTR = "target";

	/**
	 * The 'attribute' XML attribute of the 'SetAttributeValue' XML element
	 */
	public static final String TARGET_ATTRIBUTE_NAME_ATTR = "attribute";

	/**
	 * The 'newValue' XML attribute of the 'SetAttributeValue' XML element
	 */
	public static final String NEW_VALUE_ATTR = "new-value";

	private String _target = null;
	private String _targetAttributeName = null;
	private String _newValue = null;
	private Element _targetElement = null;

	public SetAttributeValue() {
	}

	@Override
	public void validate() throws SetAttributeValueException {
	}

	/**
	 * <p>
	 * Assign the given value (attribute 'new-value') to the attribute which
	 * match the given name (attribute 'attribute') of the Element Node which
	 * match the given expression (attribute 'target').
	 * </p>
	 */
	@Override
	public void doProcessing() throws SetAttributeValueException,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		synchronized (getTargetElement().getOwnerDocument()) {
			getTargetElement().setAttribute(getTargetAttributeName(),
					getNewValue());

			// Document d = getTargetElement().getOwnerDocument();
			// Element e = d.createElement("salut");
			// e.appendChild(d.createElement("node1"));
			// e.appendChild(d.createElement("node2"));
			// e.appendChild(d.createElement("node3"));
			// e.appendChild(d.createElement("node4"));
			// getTargetElement().appendChild(e);
		}
	}

	public IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	/**
	 * @return the targeted {@link Element}.
	 */
	public Element getTargetElement() {
		return _targetElement;
	}

	public Element setTargetElement(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ " (the targeted AWS Instance Element Node).");
		}
		Element previous = getTargetElement();
		_targetElement = n;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return _target;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws SetAttributeValueException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a unique XML Element node in the Resources "
					+ "Descriptor).");
		}

		NodeList nl = null;
		try {
			nl = getRD().evaluateAsNodeList(target);
		} catch (XPathExpressionException Ex) {
			throw new SetAttributeValueException(Messages.bind(
					Messages.TargetAttrEx_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new SetAttributeValueException(Messages.bind(
					Messages.TargetAttrEx_MATCH_NO_NODE, target));
		} else if (nl.getLength() > 1) {
			throw new SetAttributeValueException(Messages.bind(
					Messages.TargetAttrEx_MATCH_MANY_NODES, target,
					nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new SetAttributeValueException(Messages.bind(
					Messages.TargetAttrEx_NOT_MATCH_ELEMENT, target,
					Doc.parseNodeType(n)));
		}
		setTargetElement((Element) n);
		String previous = getTarget();
		_target = target;
		return previous;
	}

	public String getTargetAttributeName() {
		return _targetAttributeName;
	}

	@Attribute(name = TARGET_ATTRIBUTE_NAME_ATTR, mandatory = true)
	public String setTargetAttributeName(String name)
			throws SetAttributeValueException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (name.trim().length() == 0) {
			throw new SetAttributeValueException(Messages.bind(
					Messages.AttributeAttrEx_EMPTY, name));
		}
		/*
		 * FIXME : should match an attribute name pattern
		 * 
		 * for example, 'space' are forbidden
		 */
		String previous = getTargetAttributeName();
		_targetAttributeName = name;
		return previous;
	}

	public String getNewValue() {
		return _newValue;
	}

	@Attribute(name = NEW_VALUE_ATTR, mandatory = true)
	public String setNewValue(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getNewValue();
		_newValue = v;
		return previous;
	}

}