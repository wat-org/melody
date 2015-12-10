package com.wat.melody.core.internal.taskbuilder;

import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.core.nativeplugin.call.Call;
import com.wat.melody.core.nativeplugin.source.Source;

/**
 * <p>
 * Instantiate a {@link Call} based on a sequence descriptor and an order.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SourceShortcutBuilder implements ITaskBuilder {

	private OrderName _orderName;
	private ICondition _condition;
	private String _sequenceDescriptorPath;

	public SourceShortcutBuilder(OrderName o, String sdp, ICondition c) {
		setOrderName(o);
		setCondition(c);
		setSequenceDescriptorPath(sdp);
	}

	public OrderName getOrderName() {
		return _orderName;
	}

	private OrderName setOrderName(OrderName o) {
		if (o == null) {
			throw new IllegalArgumentException(
					"null: Not Accepted. " + "Must be a valid "
							+ OrderName.class.getCanonicalName() + ".");
		}
		OrderName previous = getOrderName();
		_orderName = o;
		return previous;
	}

	public ICondition getCondition() {
		return _condition;
	}

	private ICondition setCondition(ICondition ps) {
		if (ps == null) {
			throw new IllegalArgumentException(
					"null: Not Accepted. " + "Must be a valid "
							+ ICondition.class.getCanonicalName() + ".");
		}
		ICondition previous = getCondition();
		_condition = ps;
		return previous;
	}

	public String getSequenceDescriptorPath() {
		return _sequenceDescriptorPath;
	}

	private String setSequenceDescriptorPath(String sdp) {
		if (sdp == null) {
			throw new IllegalArgumentException(
					"null: Not Accepted. " + "Must be a valid "
							+ String.class.getCanonicalName() + ".");
		}
		String previous = getSequenceDescriptorPath();
		_sequenceDescriptorPath = sdp;
		return previous;
	}

	@Override
	public String getTaskName() {
		// the task name is the order name (low case)
		return getOrderName().toString().toLowerCase();
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
	public void markEligibleElements(Element elmt, PropertySet ps) {
		getCondition().markEligibleElements(elmt, ps);
	}

	@Override
	public ITask build() throws TaskFactoryException {
		Source c = new Source();
		c.setOrderName(getOrderName());
		return c;
	}

}