package com.wat.melody.core.nativeplugin.order;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
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
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.order.exception.IllegalOrderNameException;
import com.wat.melody.common.xml.NodeCollection;
import com.wat.melody.common.xml.exception.SimpleNodeRelatedException;
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
	 * Search the order whose name is equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * @param sd
	 *            is the {@link ISequenceDescriptor} to search in.
	 * 
	 * @return the found order (in its native {@link Element} format), or
	 *         <tt>null</tt> if no order {@link Element} match the given
	 *         {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link ISequenceDescriptor} is <tt>null</tt>.
	 */
	public static Element findOrder(OrderName order, ISequenceDescriptor sd) {
		NodeList nl = findOrders(order, sd);
		if (nl.getLength() == 0) {
			return null;
		} else {
			return (Element) nl.item(0);
		}
	}

	/**
	 * <p>
	 * Search for all orders whose name is equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * <p>
	 * This method is used to detect orders which have the same name.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * @param sd
	 *            is the {@link ISequenceDescriptor} to search in.
	 * 
	 * @return a {@link NodeList}, which contains all found orders (in their
	 *         native {@link Element} format).
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link ISequenceDescriptor} is <tt>null</tt>.
	 */
	public static NodeList findOrders(OrderName order, ISequenceDescriptor sd) {
		if (sd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISequenceDescriptor.class.getCanonicalName() + ".");
		}
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
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

	/**
	 * <p>
	 * Search for all orders.
	 * </p>
	 * 
	 * @param sd
	 *            is the {@link ISequenceDescriptor} to search in.
	 * 
	 * @return an {@link OrderNameSet}, which contains all found order names.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link ISequenceDescriptor} is <tt>null</tt>.
	 * 
	 * @throws IllegalOrderNameException
	 *             if one order name found is not valid.
	 */
	public static OrderNameSet findAvailableOrderNames(ISequenceDescriptor sd)
			throws IllegalOrderNameException {
		if (sd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISequenceDescriptor.class.getCanonicalName() + ".");
		}
		NodeList nl = null;
		try {
			nl = sd.evaluateAsNodeList("/*/" + ORDER + "/@" + NAME_ATTR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Since the given Order cannot contains XPath injection, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		OrderNameSet orders = new OrderNameSet();
		if (nl == null || nl.getLength() == 0) {
			throw new RuntimeException("'" + sd.getSourceFile()
					+ "': Not accepted. " + "No order declared in this "
					+ "sequence desriptor ! Impossible !");
		}
		for (int i = 0; i < nl.getLength(); i++) {
			orders.add(OrderName.parseString(nl.item(i).getNodeValue()));
		}
		return orders;

	}

	private OrderName _orderName = null;
	private String _description = null;
	private Set<Element> _innerTasks;

	public Order() {
		setInnerTasks(new LinkedHashSet<Element>());
	}

	/**
	 * <p>
	 * Search the order whose name is equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * 
	 * @return the found order (in its native {@link Element} format), or
	 *         <tt>null</tt> if no order {@link Element} match the given
	 *         {@link OrderName}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link OrderName} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the underlying {@link ISequenceDescriptor} is
	 *             <tt>null</tt>.
	 */
	public Element findOrder(OrderName order) throws IllegalOrderException {
		return findOrder(order, Melody.getContext().getProcessorManager()
				.getSequenceDescriptor());
	}

	/**
	 * <p>
	 * Search for all orders whose name is equal to the given {@link OrderName}.
	 * </p>
	 * 
	 * <p>
	 * This method is used to detect orders which have the same name.
	 * </p>
	 * 
	 * @param order
	 *            is the given {@link OrderName} to search.
	 * 
	 * @return a {@link NodeList}, which contains all found orders (in their
	 *         native {@link Element} format).
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
	 * Register the given task (in its native {@link Element} format) as an
	 * inner-task of this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	@Override
	public void registerInnerTask(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
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
			for (Element n : getInnerTasks()) {
				Melody.getContext().processTask(n);
			}
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

		NodeList nl = findOrders(name);
		int count = nl.getLength();
		if (count == 0) {
			throw new RuntimeException("Unexpected error while detecting "
					+ "duplicate order name. No order whose name is equal "
					+ "to '" + name + "' were found."
					+ "Because such order exists, such error cannot "
					+ "happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. ");
		} else if (count > 1) {
			ConsolidatedException causes = new ConsolidatedException(Msg.bind(
					Messages.OrderEx_DUPLICATE_NAME_RESUME, name, ORDER,
					NAME_ATTR));
			for (Node node : new NodeCollection(nl)) {
				causes.addCause(new SimpleNodeRelatedException(node, Msg.bind(
						Messages.OrderEx_DUPLICATE_NAME, name)));
			}
			throw new OrderException(causes);
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
	public String setDescription(String description) {
		if (description == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getDescription();
		_description = description;
		return previous;
	}

	/**
	 * @return all inner-task (in their native {@link Element} format).
	 */
	private Set<Element> getInnerTasks() {
		return _innerTasks;
	}

	private Set<Element> setInnerTasks(Set<Element> innerTasks) {
		if (innerTasks == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		Set<Element> previous = getInnerTasks();
		_innerTasks = innerTasks;
		return previous;
	}

}