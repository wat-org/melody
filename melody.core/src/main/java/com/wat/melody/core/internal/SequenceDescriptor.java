package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Node;

import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.Messages;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.common.utils.OrderName;
import com.wat.melody.common.utils.OrderNameSet;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalDocException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.common.utils.exception.MelodyException;
import com.wat.melody.core.nativeplugin.order.Order;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SequenceDescriptor extends Doc implements ISequenceDescriptor {

	private PropertiesSet moProperties;
	private OrderNameSet maOrders;
	private File msBaseDir;

	/**
	 * <p>
	 * Build an empty SequenceDescriptor and initialize all members to their
	 * default value.
	 * </p>
	 * <p>
	 * The Sequence Descriptor File contains the action's sequence to process.
	 * </p>
	 * <p>
	 * A SequenceDescriptor provide managed access to a Sequence Descriptor
	 * File's content :<BR/>
	 * - content validation ;<BR/>
	 * - content access facilities : get the Default Order, get a Property, ...
	 * ;<BR/>
	 * - querying facilities : based on XPath 2.0 expression, content can be
	 * retrieve ;<BR/>
	 * </p>
	 */
	public SequenceDescriptor() {
		super();
		initOrders();
		initBaseDir();
		initProperties();
	}

	private void initOrders() {
		maOrders = new OrderNameSet();
	}

	private void initBaseDir() {
		msBaseDir = null;
	}

	private void initProperties() {
		moProperties = new PropertiesSet();
	}

	/**
	 * <p>
	 * Initialize this object based on the given file content. Perform Orders
	 * validation, regarding the content of the given file.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't points to a valid file.
	 * @throws IllegalOrderException
	 *             if some Orders are not valid.
	 * @throws IllegalDocException
	 *             if the content of the file pointed by the given path is not
	 *             valid.
	 * @throws IOException
	 *             if an IO error occurred while reading the given file.
	 */
	@Override
	public void load(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException, IOException {
		try {
			super.load(sPath);
		} catch (IllegalDocException | IllegalFileException | IOException Ex) {
			throw Ex;
		} catch (MelodyException Ex) {
			throw new RuntimeException("Unexecpted error while loading the "
					+ "Sequence Descriptor. "
					+ "Because MelodyException cannot be raise by the "
					+ "underlying Doc, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		try {
			setBaseDir(new File(sPath).getParentFile().getCanonicalFile());
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexecpted error while setting the "
					+ "default baseDir of the Sequence Descriptor. "
					+ "Because the baseDir is the parent dir of the Sequence "
					+ "Descriptor which was previoulsy successfully loaded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		validateOrders(getOrders());
	}

	@Override
	public Property addProperty(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Property.");
		}
		return getProperties().put(p);
	}

	@Override
	public void addProperties(PropertiesSet ps) {
		getProperties().putAll(ps);
	}

	@Override
	public void addOrder(OrderName v) throws IllegalOrderException {
		if (getOrders().contains(v)) {
			throw new IllegalOrderException(Messages.bind(
					Messages.OrderEx_DUPLICATE, v, Order.ORDER));
		}
		validateOrder(v);
		maOrders.add(v);
	}

	@Override
	public void addOrders(OrderNameSet orders) throws IllegalOrderException {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid OrderNameSet.");
		}
		for (OrderName o : orders) {
			addOrder(o);
		}
	}

	@Override
	public OrderName setOrder(int i, OrderName order)
			throws IllegalOrderException {
		validateOrder(order);
		return maOrders.set(i, order);
	}

	@Override
	public int countOrders() {
		return maOrders.size();
	}

	@Override
	public OrderName getOrder(int i) {
		return maOrders.get(i);
	}

	@Override
	public void clearOrders() {
		maOrders.clear();
	}

	private void validateOrders(OrderNameSet orders)
			throws IllegalOrderException {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid OrderNameSet.");
		}
		for (OrderName v : orders) {
			validateOrder(v);
		}
	}

	private void validateOrder(OrderName v) throws IllegalOrderException {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid OrderName.");
		}
		if (getDocument() != null && Order.findOrder(v, this) == null) {
			throw new IllegalOrderException(Messages.bind(
					Messages.OrderEx_UNDEF, v));
		}
	}

	@Override
	public Node getRoot() {
		if (getDocument() == null) {
			return null;
		}
		return getDocument().getFirstChild();
	}

	private OrderNameSet getOrders() {
		return maOrders;
	}

	@Override
	public void setOrders(OrderNameSet orders) throws IllegalOrderException {
		validateOrders(orders);
		maOrders = orders;
	}

	@Override
	public File getBaseDir() {
		return msBaseDir;
	}

	@Override
	public File setBaseDir(File v) throws IllegalDirectoryException {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus ba a valid String (a Directory Path).");
		}
		Tools.validateDirExists(v.getAbsolutePath());
		File previous = getBaseDir();
		msBaseDir = v;
		return previous;
	}

	@Override
	public PropertiesSet getProperties() {
		return moProperties;
	}

	@Override
	public void setProperties(PropertiesSet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid PropertiesSet.");
		}
		moProperties = ps;
	}

}
