package com.wat.melody.core.internal;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;

import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.Messages;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.core.nativeplugin.order.Order;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SequenceDescriptor extends Doc implements ISequenceDescriptor {

	private PropertySet _properties;
	private OrderNameSet _orders;
	private File _baseDir = null;

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
		setOrders(new OrderNameSet());
		setProperties(new PropertySet());
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("source-file:");
		str.append(getSourceFile());
		str.append(", orders:");
		str.append(getOrders());
		str.append(", properties:");
		str.append(getProperties());
		str.append(" }");
		return str.toString();
	}

	@Override
	public void load(String sPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException, IOException {
		File f = new File(sPath);
		if (!f.isAbsolute()) {
			throw new IllegalFileException(Msg.bind(
					com.wat.melody.common.files.Messages.FileEx_NOT_ABSOLUTE,
					sPath));
		}
		try {
			super.load(f.toString());
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
			setBaseDir(f.getParentFile());
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
	public void load(ISequenceDescriptor sd) throws IllegalOrderException {
		try {
			setSourceFile(sd.getSourceFile());
		} catch (IllegalFileException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "setting the Doc File Path to " + "'"
					+ sd.getSourceFile() + "'. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. "
					+ "Or an external event made the file no more "
					+ "accessible (deleted, moved, read permission "
					+ "removed, ...).", Ex);
		}
		try {
			setBaseDir(sd.getBaseDir());
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexecpted error while setting the "
					+ "default baseDir of the Sequence Descriptor. "
					+ "Because the baseDir is taken from another Sequence "
					+ "Descriptor which was previously validated, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		setDocument(sd.getRoot().getOwnerDocument());
		validateOrders(getOrders());
	}

	@Override
	public PropertySet getPropertySet() {
		return (PropertySet) _properties.clone();
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
	public void addProperties(PropertySet ps) {
		getProperties().putAll(ps);
	}

	@Override
	public void setPropertySet(PropertySet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		clearProperties();
		addProperties(ps);
	}

	@Override
	public void clearProperties() {
		_properties.clear();
	}

	/**
	 * @return a shallow copy of this object's {@link OrderNameSet} (The
	 *         elements themselves are not copied; If the returned
	 *         {@link OrderNameSet} is modified, this object's
	 *         {@link OrderNameSet} will not be modified).
	 */
	@Override
	public OrderNameSet getOrderSet() {
		return (OrderNameSet) _orders.clone();
	}

	@Override
	public OrderName getOrder(int i) {
		return _orders.get(i);
	}

	@Override
	public void addOrders(OrderNameSet orders) throws IllegalOrderException {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ OrderNameSet.class.getCanonicalName() + ".");
		}
		for (OrderName o : orders) {
			addOrder(o);
		}
	}

	@Override
	public void addOrder(OrderName order) throws IllegalOrderException {
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
		}
		if (getOrders().contains(order)) {
			throw new IllegalOrderException(Msg.bind(
					Messages.OrderEx_DUPLICATE, order));
		}
		validateOrder(order);
		_orders.add(order);
	}

	@Override
	public void setOrderSet(OrderNameSet orders) throws IllegalOrderException {
		validateOrders(orders);
		clearOrders();
		addOrders(orders);
	}

	@Override
	public OrderName setOrder(int i, OrderName order)
			throws IllegalOrderException {
		validateOrder(order);
		return _orders.set(i, order);
	}

	@Override
	public void clearOrders() {
		_orders.clear();
	}

	@Override
	public OrderName removeOrder(int i) {
		return _orders.remove(i);
	}

	@Override
	public int countOrders() {
		return _orders.size();
	}

	private void validateOrders(OrderNameSet orders)
			throws IllegalOrderException {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ OrderNameSet.class.getCanonicalName() + ".");
		}
		for (OrderName v : orders) {
			validateOrder(v);
		}
	}

	private void validateOrder(OrderName order) throws IllegalOrderException {
		if (order == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
		}
		if (getDocument() != null && Order.findOrder(order, this) == null) {
			throw new IllegalOrderException(Msg.bind(Messages.OrderEx_UNDEF,
					order));
		}
	}

	@Override
	public Element getRoot() {
		if (getDocument() == null) {
			return null;
		}
		return (Element) getDocument().getFirstChild();
	}

	@Override
	public File getBaseDir() {
		return _baseDir;
	}

	@Override
	public File setBaseDir(File dir) throws IllegalDirectoryException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + File.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		FS.validateDirExists(dir.toString());
		File previous = getBaseDir();
		_baseDir = dir;
		return previous;
	}

	private OrderNameSet getOrders() {
		return _orders;
	}

	private OrderNameSet setOrders(OrderNameSet orders) {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ OrderNameSet.class.getCanonicalName() + ".");
		}
		OrderNameSet previous = getOrders();
		_orders = orders;
		return previous;
	}

	private PropertySet getProperties() {
		return _properties;
	}

	private PropertySet setProperties(PropertySet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		PropertySet previous = getProperties();
		_properties = ps;
		return previous;
	}

}