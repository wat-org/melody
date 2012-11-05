package com.wat.melody.core.nativeplugin.call;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.utils.OrderName;
import com.wat.melody.common.utils.OrderNameSet;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.exception.IllegalDocException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.core.nativeplugin.call.exception.CallException;

/**
 * <p>
 * This class stores data which are necessary for a {@link Call} Task to launch
 * the processing of Orders defined in another Sequence Descriptor file.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Ref {

	/**
	 * The 'ref' nested element of the 'call' XML element
	 */
	public static final String REF = "ref";

	/**
	 * The 'orders' XML attribute of the 'ref' or 'call' XML element
	 */
	public static final String ORDERS_ATTR = "orders";

	/**
	 * The 'sequenceDescriptor' XML attribute of the 'ref' or 'call' XML element
	 */
	public static final String SD_ATTR = "sequenceDescriptor";

	/**
	 * The 'param' nested elment of the 'ref' or 'call' XML element
	 */
	public static final String PARAM = "param";

	private Call moRelatedCall;
	private String moSDPath;
	private List<IProcessorManager> maIProcessorManagers;

	protected Ref() {
		this(null);
	}

	/**
	 * <p>
	 * Create a new {@link Ref} object, which will contains one
	 * sub-ProcessorManager per Order.
	 * </p>
	 * <p>
	 * <i>* A new sub-ProcessorManager will be created for each Order. <BR/>
	 * * The given IProcessorManager will be used as a model for the
	 * sub-ProcessorManager creation. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param c
	 *            is the parent Call Task in which this new CallRef nested
	 *            element is defined.
	 */
	public Ref(Call c) {
		setIProcessorManagers(new ArrayList<IProcessorManager>());
		setRelatedCall(c);
	}

	/**
	 * <p>
	 * Load the content of the file points by the given path into all
	 * sub-ProcessorManager owned by this object.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to load.
	 * 
	 * @throws CallException
	 *             if the given path doesn't point to a valid file (e.g. the
	 *             file doesn't exists, the path is a directory, ...).
	 * @throws CallException
	 *             if the content of the file pointed by the given path is not a
	 *             valid Sequence Descriptor.
	 * @throws CallException
	 *             if one order is not valid (e.g. doesn't respect the order
	 *             syntax, doesn't match any order defined in the Sequence
	 *             Descriptor file, ...).
	 * @throws IOException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             if the given path is <code>null</code>.
	 */
	@Attribute(name = SD_ATTR)
	public void setSequenceDescriptor(File sPath) throws CallException,
			IOException {
		setSDPath(sPath.getCanonicalPath());
		try {
			for (IProcessorManager pm : getIProcessorManagers()) {
				pm.getSequenceDescriptor().load(getSDPath());
			}
		} catch (IllegalOrderException | IllegalDocException
				| IllegalFileException Ex) {
			throw new CallException(Ex);
		}
	}

	/**
	 * <p>
	 * Add the given Property into all sub-ProcessorManager owned by this
	 * object.
	 * </p>
	 * 
	 * @param p
	 *            is the Property to add.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given property is <code>null</code>.
	 */
	@NestedElement(name = PARAM, type = NestedElement.Type.ADD)
	public void addParam(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Property.");
		}
		for (IProcessorManager pm : getIProcessorManagers()) {
			pm.getSequenceDescriptor().addProperty(p);
		}
	}

	/**
	 * <p>
	 * Create one sub-ProcessorManager per order in the given order list.
	 * </p>
	 * 
	 * @param orders
	 *            is an order list (comma separated list).
	 * 
	 * @throws CallException
	 *             if one order is not valid (e.g. doesn't respect the order
	 *             syntax, doesn't match any order defined in the Sequence
	 *             Descriptor file, ...).
	 * @throws IOException
	 */
	@Attribute(name = ORDERS_ATTR)
	public void setOrders(OrderNameSet orders) throws CallException,
			IOException {
		IProcessorManager pm = null;
		pm = getRelatedCall().getContext().getProcessorManager();
		PropertiesSet ps = getRelatedCall().getContext().getProperties();
		try {
			for (OrderName order : orders) {
				IProcessorManager spm = pm.createSubProcessorManager(ps);
				addIProcessorManager(spm);
				if (getSDPath() != null) {
					spm.getSequenceDescriptor().load(getSDPath());
				}
				spm.getSequenceDescriptor().addOrder(order);
			}
		} catch (IllegalOrderException | IllegalDocException
				| IllegalFileException Ex) {
			throw new CallException(Ex);
		}
	}

	private Call getRelatedCall() {
		return moRelatedCall;
	}

	protected Call setRelatedCall(Call c) {
		// Can be null
		Call previous = getRelatedCall();
		moRelatedCall = c;
		return previous;
	}

	protected List<IProcessorManager> getIProcessorManagers() {
		return maIProcessorManagers;
	}

	private void setIProcessorManagers(List<IProcessorManager> o) {
		maIProcessorManagers = o;
	}

	private String getSDPath() {
		return moSDPath;
	}

	private String setSDPath(String sPath) {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (the Sequence Descriptor file "
					+ "path).");
		}
		String previous = getSDPath();
		moSDPath = sPath;
		return previous;
	}

	protected IProcessorManager getIProcessorManager(int i) {
		return getIProcessorManagers().get(i);
	}

	protected void addIProcessorManager(IProcessorManager pm) {
		getIProcessorManagers().add(pm);
	}

}