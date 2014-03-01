package com.wat.melody.core.nativeplugin.attributes;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.AttributeName;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.core.nativeplugin.attributes.exception.SetAttributeValueException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = SetAttributeValue.SET_ATTRIBUTE_VALUE)
public class SetAttributeValue implements ITask {

	/**
	 * Task's name
	 */
	public static final String SET_ATTRIBUTE_VALUE = "set-attribute-value";

	/**
	 * Task's attribute, which specifies the targeted {@link Element}.
	 */
	public static final String TARGET_ELEMENT_ATTR = "target-element";

	/**
	 * Task's attribute, which specifies the targeted {@link Element}'s
	 * {@link Attr} to set.
	 */
	public static final String TARGET_ATTRIBUTE_NAME_ATTR = "target-attribute";

	/**
	 * Task's attribute, which specifies the value to assign to the targeted
	 * {@link Element}'s {@link Attr} to set.
	 */
	public static final String NEW_VALUE_ATTR = "new-value";

	private String _target = null;
	private AttributeName _targetAttributeName = null;
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
	public void doProcessing() throws InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		synchronized (getTargetElement().getOwnerDocument()) {
			getTargetElement().setAttribute(
					getTargetAttributeName().getValue(), getNewValue());
		}
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
					+ " (the targeted XML Element Node).");
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

	@Attribute(name = TARGET_ELEMENT_ATTR, mandatory = true)
	public String setTarget(String target) throws SetAttributeValueException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a unique XML Element Node in the Resources "
					+ "Descriptor).");
		}

		NodeList nl = null;
		try {
			nl = Melody.getContext().getProcessorManager()
					.getResourcesDescriptor().evaluateAsNodeList(target);
		} catch (XPathExpressionException Ex) {
			throw new SetAttributeValueException(Msg.bind(
					Messages.TargetAttrEx_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new SetAttributeValueException(Msg.bind(
					Messages.TargetAttrEx_MATCH_NO_NODE, target));
		} else if (nl.getLength() > 1) {
			throw new SetAttributeValueException(Msg.bind(
					Messages.TargetAttrEx_MATCH_MANY_NODES, target,
					nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new SetAttributeValueException(Msg.bind(
					Messages.TargetAttrEx_NOT_MATCH_ELEMENT, target,
					DocHelper.parseNodeType(n)));
		}
		setTargetElement((Element) n);
		String previous = getTarget();
		_target = target;
		return previous;
	}

	public AttributeName getTargetAttributeName() {
		return _targetAttributeName;
	}

	@Attribute(name = TARGET_ATTRIBUTE_NAME_ATTR, mandatory = true)
	public AttributeName setTargetAttributeName(AttributeName name) {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ AttributeName.class.getCanonicalName() + ".");
		}
		AttributeName previous = getTargetAttributeName();
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