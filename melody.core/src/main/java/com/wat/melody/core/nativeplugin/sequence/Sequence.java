package com.wat.melody.core.nativeplugin.sequence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.Property;
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

	private ITaskContext moContext;
	private File msBaseDir;
	private OrderName msDefault;
	private String msDescription;
	private List<Node> maNodes;

	public Sequence() {
		initContext();
		initBaseDir();
		initDefault();
		initDescription();
		initNodes();
	}

	private void initContext() {
		moContext = null;
	}

	private void initBaseDir() {
		msBaseDir = null;
	}

	private void initDefault() {
		msDefault = null;
	}

	private void initDescription() {
		msDescription = null;
	}

	private void initNodes() {
		maNodes = new ArrayList<Node>();
	}

	@NestedElement(name = Property.PROPERTY, type = NestedElement.Type.ADD)
	public void addProperty(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Property.");
		}
		// If the property already exists => do not replace it !!
		// Note: Injected Properties predates declared property in the sequence
		// task (and property can be injected via the command line or via the
		// configuration file)
		if (getContext().getProperties().containsKey(p.getName().getValue())) {
			return;
		}
		getContext().getProperties().put(p);
	}

	/**
	 * <p>
	 * Register the given Task (in its native Node format) as an inner Task of
	 * this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given node is <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given node is already registered.
	 * 
	 */
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

	/**
	 * <p>
	 * Validate this object.
	 * </p>
	 * <p>
	 * <i> * Set the {@link SequenceDescriptor}'s baseDir to the baseDir default
	 * value if it was not defined. <BR/>
	 * * The baseDir default value is the parent folder of the Sequence
	 * Descriptor file. <BR/>
	 * </i>
	 * </p>
	 * 
	 */
	@Override
	public void validate() {
	}

	/**
	 * <p>
	 * Process the {@link Sequence} Task.
	 * </p>
	 * <p>
	 * <i> * All orders defined in the Sequence Descriptor orders' list are
	 * proceed one by one. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws SequenceException
	 *             if an error occurred during processing.
	 * 
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 * 
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 * 
	 */
	@Override
	public void doProcessing() throws SequenceException, InterruptedException {
		try {
			for (int i = 0; i < getContext().getProcessorManager()
					.getSequenceDescriptor().countOrders(); i++) {
				processOrder(getContext().getProcessorManager()
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
		for (Node n : getNodes()) {
			if (n.getNodeName().equalsIgnoreCase(Order.class.getSimpleName())
					&& n.getAttributes().getNamedItem(Order.NAME_ATTR)
							.getNodeValue().equals(order.getValue())) {
				getContext().processTask(n);
				return;
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

	public File getBaseDir() {
		return msBaseDir;
	}

	@Attribute(name = BASEDIR_ATTR)
	public File setBaseDir(File name) throws SequenceException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		try {
			getContext().getProcessorManager().getSequenceDescriptor()
					.setBaseDir(name);
		} catch (IllegalDirectoryException Ex) {
			throw new SequenceException(Ex);
		}
		File previous = getBaseDir();
		msBaseDir = name;
		return previous;
	}

	public OrderName getDefault() {
		return msDefault;
	}

	/**
	 * <p>
	 * Validate the given name. If no Order has been explicitly loaded into the
	 * {@link SequenceDescriptor}, will load the Order which correspond to the
	 * given name.
	 * </p>
	 * 
	 * @param order
	 *            is the name of the default Order.
	 * 
	 * @throws SequenceException
	 *             if the given name doesn't refer to the name of an Order
	 *             defined in the Sequence Descriptor.
	 * 
	 * @throws SequenceException
	 *             if the given name is not a valid Order name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code>.
	 * 
	 */
	@Attribute(name = DEFAULT_ATTR)
	public OrderName setDefault(OrderName order) throws SequenceException {
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (getContext().getProcessorManager().getSequenceDescriptor()
				.countOrders() == 0) {
			try {
				getContext().getProcessorManager().getSequenceDescriptor()
						.addOrder(order);
			} catch (IllegalOrderException Ex) {
				throw new SequenceException(Ex);
			}
		}
		OrderName previous = getDefault();
		msDefault = order;
		return previous;
	}

	public String getDescription() {
		return msDescription;
	}

	/**
	 * <p>
	 * Set the Description with the given value.
	 * </p>
	 * 
	 * @param v
	 *            is the description to set.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given value is <code>null</code>.
	 * 
	 */
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

	/**
	 * <p>
	 * Get all inner Task (in their native Node format) of this object.
	 * </p>
	 * 
	 * @return all inner Task (in their native Node format)
	 * 
	 */
	private List<Node> getNodes() {
		return maNodes;
	}

}