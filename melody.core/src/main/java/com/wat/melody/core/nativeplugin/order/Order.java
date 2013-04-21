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
import com.wat.melody.api.Melody;
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
	 *             if the given input {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given input {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
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
	 * @param sd
	 *            is the Sequence Descriptor to search in.
	 * 
	 * @return the Order Element <code>NodeList</code> whose name match the
	 *         given input {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given input {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
	 */
	public static NodeList findOrders(OrderName order, ISequenceDescriptor sd) {
		if (sd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid SequenceDescriptor.");
		}
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Order).");
		}
		try {
			return sd.evaluateAsNodeList("/*/" + ORDER + "[@" + NAME_ATTR
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

	private OrderName _orderName = null;
	private String _description = null;
	private List<Node> _innerTasks;

	public Order() {
		setInnerTasks(new ArrayList<Node>());
	}

	/**
	 * <p>
	 * Search for order whose name is equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * 
	 * @return the found order (in its native {@link Node} format).
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
	 */
	public Node findOrder(OrderName order) throws IllegalOrderException {
		return findOrder(order, Melody.getContext().getProcessorManager()
				.getSequenceDescriptor());
	}

	/**
	 * <p>
	 * Search for order whose name are equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * 
	 * @return the found orders (in their native {@link Node} format).
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
	 */
	public NodeList findOrders(OrderName order) {
		return findOrders(order, Melody.getContext().getProcessorManager()
				.getSequenceDescriptor());
	}

	/**
	 * <p>
	 * Register the given Task (in its native Node format) as an inner-task of
	 * this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given node is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given node is already registered.
	 */
	@Override
	public void registerInnerTask(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (_innerTasks.contains(n)) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Node already present in list.");
		}
		_innerTasks.add(n);
	}

	@Override
	public void validate() {
		// almost nothing to do
	}

	/**
	 * <p>
	 * Process all inner-tasks registered in this object.
	 * </p>
	 * 
	 * @throws OrderException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 */
	@Override
	public void doProcessing() throws OrderException, InterruptedException {
		try {
			for (Node n : getInnerTasks()) {
				Melody.getContext().processTask(n);
			}
		} catch (InterruptedException Ex) {
			throw new InterruptedException(Messages.bind(
					Messages.OrderEx_INTERRUPTED, ORDER, getName()));
		} catch (TaskException Ex) {
			throw new OrderException(Ex);
		}
	}

	public OrderName getName() {
		return _orderName;
	}

	/**
	 * <p>
	 * Assign the given name to this object.
	 * </p>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @return the previous name of this object.
	 * 
	 * @throws OrderException
	 *             if the given name is not unique in the underlying
	 *             {@link ISequenceDescriptor}.
	 * @throws IllegalArgumentException
	 *             if the given input {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
	 */
	@Attribute(name = NAME_ATTR, mandatory = true)
	public OrderName setName(OrderName name) throws OrderException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
		}

		int count = findOrders(name).getLength();
		if (count == 0) {
			throw new RuntimeException("Unexpected error while detecting "
					+ "duplicate order name. No order whose name is equal "
					+ "to " + name + " were found."
					+ "Because such order exists, such error cannot "
					+ "happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. ");
		} else if (count > 1) {
			throw new OrderException(Messages.bind(
					Messages.OrderEx_DUPLICATE_NAME, new Object[] { name,
							ORDER, NAME_ATTR }));
		}
		OrderName previous = getName();
		_orderName = name;
		return previous;
	}

	public String getDescription() {
		return _description;
	}

	/**
	 * <p>
	 * Set the description with the given value.
	 * </p>
	 * 
	 * @param description
	 *            is the description to set.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given value is <tt>null</tt>.
	 */
	@Attribute(name = DESCRIPTION_ATTR)
	public String setDescription(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getDescription();
		_description = v;
		return previous;
	}

	/**
	 * <p>
	 * Get all inner-tasks (in their native {@link Node} format) of this task.
	 * </p>
	 * 
	 * @return all inner-task (in their native {@link Node} format).
	 */
	private List<Node> getInnerTasks() {
		return _innerTasks;
	}

	private List<Node> setInnerTasks(List<Node> innerTasks) {
		if (innerTasks == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Node>.");
		}
		List<Node> previous = getInnerTasks();
		_innerTasks = innerTasks;
		return previous;
	}

}