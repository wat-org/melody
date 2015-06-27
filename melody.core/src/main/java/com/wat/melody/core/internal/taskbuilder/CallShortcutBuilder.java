package com.wat.melody.core.internal.taskbuilder;

import java.io.IOException;

import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.files.WrapperFile;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.core.nativeplugin.call.Call;
import com.wat.melody.core.nativeplugin.call.exception.CallException;

/**
 * <p>
 * Instantiate a {@link Call} based on a sequence descriptor and an order.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class CallShortcutBuilder implements ITaskBuilder {

	private OrderName _order;
	private ICondition _condition;
	private String _sequenceDescriptorPath;

	public CallShortcutBuilder(OrderName o, String sdp, ICondition c) {
		setOrder(o);
		setCondition(c);
		setSequenceDescriptorPath(sdp);
	}

	private OrderName getOrder() {
		return _order;
	}

	private OrderName setOrder(OrderName o) {
		if (o == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + OrderName.class.getCanonicalName()
					+ ".");
		}
		OrderName previous = getOrder();
		_order = o;
		return previous;
	}

	private ICondition getCondition() {
		return _condition;
	}

	private ICondition setCondition(ICondition ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + ICondition.class.getCanonicalName()
					+ ".");
		}
		ICondition previous = getCondition();
		_condition = ps;
		return previous;
	}

	private String getSequenceDescriptorPath() {
		return _sequenceDescriptorPath;
	}

	private String setSequenceDescriptorPath(String sdp) {
		if (sdp == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getSequenceDescriptorPath();
		_sequenceDescriptorPath = sdp;
		return previous;
	}

	@Override
	public String getTaskName() {
		// the task name is the order name (low case)
		return getOrder().toString().toLowerCase();
	}

	@Override
	public Class<? extends ITask> getTaskClass() {
		return Call.class;
	}

	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		return getCondition().isEligible(elmt, ps);
	}

	@Override
	public ITask build() throws TaskFactoryException {
		Call c = new Call();
		try {
			OrderNameSet ons = new OrderNameSet();
			ons.add(getOrder());
			c.setOrders(ons);
			c.setSequenceDescriptor(new WrapperFile(getSequenceDescriptorPath()));
		} catch (CallException Ex) {
			throw new TaskFactoryException("shouldn't happened.", Ex);
		} catch (IOException Ex) {
			throw new TaskFactoryException("fucked up!!", Ex);
		}
		return c;
	}

}