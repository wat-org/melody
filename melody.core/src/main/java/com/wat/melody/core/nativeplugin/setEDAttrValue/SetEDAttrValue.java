package com.wat.melody.core.nativeplugin.setEDAttrValue;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.core.nativeplugin.setEDAttrValue.exception.SetEDAttrValueException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SetEDAttrValue implements ITask {

	/**
	 * The 'sequence' XML element used in the Sequence Descriptor
	 */
	public static final String UPDATE_ED_ATTR = "SetEDAttrValue";

	/**
	 * The 'items' XML attribute of the 'SetEDAttrValue' XML element
	 */
	public static final String ITEMS_ATTR = "items";

	/**
	 * The 'newValue' XML attribute of the 'SetEDAttrValue' XML element
	 */
	public static final String NEW_VALUE_ATTR = "new-value";

	private String _items = null;
	private String _newValue = null;
	private NodeList _attributesToUpdate = null;

	public SetEDAttrValue() {
	}

	@Override
	public void validate() throws SetEDAttrValueException {
	}

	/**
	 * <p>
	 * Assign the given value (attribute 'new-value') to attributes which match
	 * the given expression (attribute 'items').
	 * </p>
	 */
	@Override
	public void doProcessing() throws SetEDAttrValueException,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		for (int i = 0; i < getAttributesToUpdate().getLength(); i++) {
			Attr a = (Attr) getAttributesToUpdate().item(i);
			synchronized (a.getOwnerDocument()) {
				a.setValue(getNewValue());
			}
		}
	}

	public String getItems() {
		return _items;
	}

	@Attribute(name = ITEMS_ATTR, mandatory = true)
	public void setItems(String items) throws SetEDAttrValueException {
		if (items == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (items.trim().length() == 0) {
			throw new SetEDAttrValueException(Messages.bind(
					Messages.SetEDAttrEx_UPDATE_ED_ITEMS_EMPTY, items));
		}

		IResourcesDescriptor env = Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
		try {
			setAttributesToUpdate(env.evaluateAsNodeList(items));
		} catch (XPathExpressionException Ex) {
			throw new SetEDAttrValueException(Messages.bind(
					Messages.SetEDAttrEx_UPDATE_ED_ITEMS_INVALID_XPATH, items),
					Ex);
		}
		for (int i = 0; i < getAttributesToUpdate().getLength(); i++) {
			if (getAttributesToUpdate().item(i).getNodeType() != Node.ATTRIBUTE_NODE) {
				throw new SetEDAttrValueException(Messages.bind(
						Messages.SetEDAttrEx_UPDATE_ED_ITEMS_INVALID_TARGET,
						items));
			}
		}
		_items = items;
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

	private NodeList getAttributesToUpdate() {
		return _attributesToUpdate;
	}

	private void setAttributesToUpdate(NodeList nl) {
		if (nl == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + NodeList.class.getCanonicalName()
					+ ".");
		}
		_attributesToUpdate = nl;
	}

}