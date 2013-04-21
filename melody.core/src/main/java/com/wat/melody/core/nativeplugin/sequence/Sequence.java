package com.wat.melody.core.nativeplugin.sequence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.core.internal.SequenceDescriptor;
import com.wat.melody.core.nativeplugin.order.Order;
import com.wat.melody.core.nativeplugin.sequence.exception.SequenceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Sequence implements ITask, ITaskContainer, ITopLevelTask {

	/**
	 * The 'sequence' XML element used in the Sequence Descriptor
	 */
	public static final String SEQUENCE = "sequence";

	/**
	 * The 'basedir' XML attribute of the 'sequence' XML element
	 */
	public static final String BASEDIR_ATTR = "basedir";

	/**
	 * The 'default' XML attribute of the 'sequence' XML element
	 */
	public static final String DEFAULT_ATTR = "default";

	/**
	 * The 'description' XML attribute of the 'order' XML element
	 */
	public static final String DESCRIPTION_ATTR = "description";

	private File _baseDir = null;
	private OrderName _defaultOrder = null;
	private String _description = null;
	private List<Node> _innerTasks;

	public Sequence() {
		setInnerTasks(new ArrayList<Node>());
	}

	/**
	 * <p>
	 * Register the given {@link Property} in the current {@link ITaskContext},
	 * so that it's {@link PropertyName} can be used during variable expansion.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>If the given {@link Property} already exists in the current
	 * {@link ITaskContext}, it will not be registered (because injected
	 * properties - via the command line or via the configuration file -
	 * predates) ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param p
	 *            is the {@link Property} to register.
	 */
	@NestedElement(name = Property.PROPERTY, type = NestedElement.Type.ADD)
	public void addProperty(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Property.");
		}
		// If the property already exists => do not replace it !!
		if (Melody.getContext().getProperties()
				.containsKey(p.getName().getValue())) {
			return;
		}
		Melody.getContext().getProperties().put(p);
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
	 * Process all orders defined in the Sequence Descriptor orders's list, one
	 * by one.
	 * </p>
	 * 
	 * @throws SequenceException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 */
	@Override
	public void doProcessing() throws SequenceException, InterruptedException {
		try {
			for (int i = 0; i < Melody.getContext().getProcessorManager()
					.getSequenceDescriptor().countOrders(); i++) {
				processOrder(Melody.getContext().getProcessorManager()
						.getSequenceDescriptor().getOrder(i));
			}
		} catch (InterruptedException Ex) {
			throw new InterruptedException(Messages.bind(
					Messages.SequenceEx_INTERRUPTED, SEQUENCE));
		} catch (TaskException Ex) {
			throw new SequenceException(Ex);
		}
	}

	private void processOrder(OrderName order) throws TaskException,
			InterruptedException {
		for (Node n : getInnerTasks()) {
			if (n.getNodeName().equalsIgnoreCase(Order.class.getSimpleName())
					&& n.getAttributes().getNamedItem(Order.NAME_ATTR)
							.getNodeValue().equals(order.getValue())) {
				Melody.getContext().processTask(n);
				return;
			}
		}
	}

	public File getBaseDir() {
		return _baseDir;
	}

	/**
	 * <p>
	 * Register the given path as the {@link SequenceDescriptor}'s baseDir.
	 * </p>
	 * 
	 * @throws SequenceException
	 *             if the given path doesn't point to a valid Directory.
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 */
	@Attribute(name = BASEDIR_ATTR)
	public File setBaseDir(File path) throws SequenceException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		try {
			Melody.getContext().getProcessorManager().getSequenceDescriptor()
					.setBaseDir(path);
		} catch (IllegalDirectoryException Ex) {
			throw new SequenceException(Ex);
		}
		File previous = getBaseDir();
		_baseDir = path;
		return previous;
	}

	public OrderName getDefault() {
		return _defaultOrder;
	}

	/**
	 * <p>
	 * Register the given order as the default order.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>If no order have been registered into the {@link SequenceDescriptor},
	 * this default order will be proceed ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param order
	 *            is the name of the default Order.
	 * 
	 * @throws SequenceException
	 *             if the given order doesn't refer to the name of an Order
	 *             defined in the Sequence Descriptor.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 */
	@Attribute(name = DEFAULT_ATTR)
	public OrderName setDefault(OrderName order) throws SequenceException {
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
		}
		// If no order have been registered, the default order will be proceed.
		if (Melody.getContext().getProcessorManager().getSequenceDescriptor()
				.countOrders() == 0) {
			try {
				Melody.getContext().getProcessorManager()
						.getSequenceDescriptor().addOrder(order);
			} catch (IllegalOrderException Ex) {
				throw new SequenceException(Ex);
			}
		}
		OrderName previous = getDefault();
		_defaultOrder = order;
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
					+ "Must be a valid String.");
		}
		String previous = getDescription();
		_description = description;
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