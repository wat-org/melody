package com.wat.melody.core.nativeplugin.setEDAttrValue;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;
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
	public static final String NEW_VALUE_ATTR = "newValue";

	private ITaskContext moContext;
	private String msItems;
	private String msNewValue;
	private NodeList moAttributesToUpdate;

	public SetEDAttrValue() {
		initContext();
		initItems();
		initNewValue();
		initAttributes();
	}

	private void initContext() {
		moContext = null;
	}

	private void initItems() {
		msItems = null;
	}

	private void initNewValue() {
		msNewValue = null;
	}

	private void initAttributes() {
		moAttributesToUpdate = null;
	}

	@Override
	public void validate() throws SetEDAttrValueException {
	}

	@Override
	public void doProcessing() throws SetEDAttrValueException,
			InterruptedException {
		getContext().handleProcessorStateUpdates();

		IResourcesDescriptor env = getContext().getProcessorManager()
				.getResourcesDescriptor();

		for (int i = 0; i < getAttributesToUpdate().getLength(); i++) {
			Attr a = (Attr) getAttributesToUpdate().item(i);
			DUNID sOwnerNodeMelodyID = env.getMelodyID(a.getOwnerElement());
			try {
				env.setAttributeValue(sOwnerNodeMelodyID, a.getNodeName(),
						getNewValue());
			} catch (NoSuchDUNIDException Ex) {
				throw new RuntimeException("Node '" + sOwnerNodeMelodyID
						+ "' can not be found in the Resources Descriptor.", Ex);
			}
		}
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;
	}

	public String getItems() {
		return msItems;
	}

	@Attribute(name = ITEMS_ATTR, mandatory = true)
	public void setItems(String items) throws SetEDAttrValueException {
		if (items == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (items.trim().length() == 0) {
			throw new SetEDAttrValueException(Messages.bind(
					Messages.SetEDAttrEx_UPDATE_ED_ITEMS_EMPTY, items));
		}

		IResourcesDescriptor env = getContext().getProcessorManager()
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
			Attr a = (Attr) getAttributesToUpdate().item(i);
			if (env.getMelodyID(a.getOwnerElement()) == null) {
				throw new RuntimeException("Cannot found the DUNID XML "
						+ "attribute of a Node. "
						+ "Because all Nodes should contains a DUNID XML "
						+ "attribute, such error shouldn't be raised. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.");
			}
		}
		msItems = items;
	}

	public String getNewValue() {
		return msNewValue;
	}

	@Attribute(name = NEW_VALUE_ATTR, mandatory = true)
	public String setNewValue(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getNewValue();
		msNewValue = v;
		return previous;
	}

	private NodeList getAttributesToUpdate() {
		return moAttributesToUpdate;
	}

	private void setAttributesToUpdate(NodeList nl) {
		if (nl == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid NodeList (a XML Attribute list).");
		}
		moAttributesToUpdate = nl;
	}

}