package com.wat.melody.core.nativeplugin.order;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IFirstLevelTask;
import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.core.nativeplugin.order.exception.OrderException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Order implements ITask, ITaskContainer, IFirstLevelTask {

	/**
	 * The 'order' XML element used in the Sequence Descriptor
	 */
	public static final String ORDER = "order";

	/**
	 * The 'name' XML attribute of the 'order' XML element
	 */
	public static final String NAME_ATTR = "name";

	/**
	 * The 'description' XML attribute of the 'order' XML element
	 */
	public static final String DESCRIPTION_ATTR = "description";

	/**
	 * <p>
	 * Search the Order whose name is equal to the given input {@link OrderName}
	 * .
	 * </p>
	 * 
	 * @param order
	 *            is the given input {@link OrderName}.
	 * @param SD
	 *            is the Sequence Descriptor to search in.
	 * 
	 * @return the Order Element <code>Node</code> whose name match the given
	 *         input {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given input {@link ISequenceDescriptor} is
	 *             <code>null</code>.
	 */
	public static Node findOrder(OrderName order, ISequenceDescriptor SD) {
		NodeList nl = findOrders(order, SD);
		if (nl.getLength() == 0) {
			return null;
		} else {
			return nl.item(0);
		}
	}

	/**
	 * <p>
	 * Search all Orders whose name are equal to the given input
	 * {@link OrderName} .
	 * </p>
	 * 
	 * @param order
	 *            is the given input {@link OrderName}.
	 * @param SD
	 *            is the Sequence Descriptor to search in.
	 * 
	 * @return the Order Element <code>NodeList</code> whose name match the
	 *         given input {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given input {@link ISequenceDescriptor} is
	 *             <code>null</code>.
	 */
	public static NodeList findOrders(OrderName order, ISequenceDescriptor SD) {
		if (SD == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid SequenceDescriptor.");
		}
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Order).");
		}
		try {
			return SD.evaluateAsNodeList("/*/" + ORDER + "[@" + NAME_ATTR
					+ "='" + order + "']");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Since the given Order cannot contains XPath injection, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private ITaskContext moContext;
	private OrderName msName;
	private String msDescription;
	private List<Node> maNodes;

	public Order() {
		initContext();
		initName();
		initDescription();
		initNodes();
	}

	private void initContext() {
		moContext = null;
	}

	private void initName() {
		msName = null;
	}

	private void initDescription() {
		msDescription = null;
	}

	private void initNodes() {
		maNodes = new ArrayList<Node>();
	}

	/**
	 * <p>
	 * Search the Order whose name is equal to the given input {@link OrderName}
	 * .
	 * </p>
	 * 
	 * @param order
	 *            is the given input {@link OrderName}.
	 * 
	 * @return the Order Element <code>Node</code> whose name match the given
	 *         input {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <code>null</code>.
	 * 
	 */
	public Node findOrder(OrderName order) throws IllegalOrderException {
		return findOrder(order, getContext().getProcessorManager()
				.getSequenceDescriptor());
	}

	/**
	 * <p>
	 * Search all Orders whose name are equal to the given input
	 * {@link OrderName} .
	 * </p>
	 * 
	 * @param order
	 *            is the given input {@link OrderName}.
	 * 
	 * @return the Order Element <code>NodeList</code> whose name match the
	 *         given input {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <code>null</code>.
	 */
	public NodeList findOrders(OrderName order) throws IllegalOrderException {
		return findOrders(order, getContext().getProcessorManager()
				.getSequenceDescriptor());
	}

	@Override
	public void addNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (maNodes.contains(n)) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Node already present in list.");
		}
		maNodes.add(n);
	}

	@Override
	public void validate() {
	}

	@Override
	public void doProcessing() throws OrderException, InterruptedException {
		try {
			for (Node n : getNodes()) {
				getContext().processTask(n);
			}
		} catch (InterruptedException Ex) {
			throw new InterruptedException(Messages.bind(
					Messages.OrderEx_INTERRUPTED, ORDER, getName()));
		} catch (TaskException Ex) {
			throw new OrderException(Ex);
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

	public OrderName getName() {
		return msName;
	}

	@Attribute(name = NAME_ATTR, mandatory = true)
	public OrderName setName(OrderName name) throws OrderException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}

		try {
			if (findOrders(name).getLength() != 1) {
				throw new OrderException(Messages.bind(
						Messages.OrderEx_DUPLICATE_NAME, new Object[] { name,
								ORDER, NAME_ATTR }));
			}
		} catch (IllegalOrderException Ex) {
			throw new OrderException(Ex);
		}
		OrderName previous = getName();
		msName = name;
		return previous;
	}

	public String getDescription() {
		return msDescription;
	}

	@Attribute(name = DESCRIPTION_ATTR)
	public String setDescription(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getDescription();
		msDescription = v;
		return previous;
	}

	private List<Node> getNodes() {
		return maNodes;
	}

}