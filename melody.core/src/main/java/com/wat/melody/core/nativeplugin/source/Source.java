package com.wat.melody.core.nativeplugin.source;

import java.io.IOException;

import org.w3c.dom.Element;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.IUnexpectedAttributes;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.ProcessorManagerConfigurationException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.xml.exception.IllegalDocException;
import com.wat.melody.core.internal.taskbuilder.SourceShortcutBuilder;
import com.wat.melody.core.nativeplugin.order.Order;
import com.wat.melody.core.nativeplugin.source.exception.SourceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Source implements ITask, IUnexpectedAttributes {

	/**
	 * The 'source' XML element used in the Sequence Descriptor
	 */
	public static final String SOURCE = "source";

	/**
	 * The 'order' XML attribute of the 'source' XML element
	 */
	public static final String ORDER_ATTR = "order";

	/**
	 * The 'param' XML Nested Elment
	 */
	public static final String PARAM = "param";

	private OrderName _orderName = null;

	@Override
	public void validate() throws SourceException {
		// As explained in setOrderName, the attribute is not annotated as
		// mandatory.
		// So we have to validate it has been set here.
		if (getOrderName() == null) {
			throw new SourceException(
					"Mandatory XML attribute '" + ORDER_ATTR + "' missing");
		}
	}

	@Override
	public void doProcessing() throws SourceException, InterruptedException {
		ITaskBuilder tb = Melody.getContext().getProcessorManager()
				.getRegisteredTasks()
				.retrieveEligibleTaskBuilder(getOrderName().toString(),
						Melody.getContext().getRelatedElement(),
						Melody.getContext().getProperties());
		if (!(tb instanceof SourceShortcutBuilder)) {
			return;
		}
		SourceShortcutBuilder csb = (SourceShortcutBuilder) tb;
		if (csb.getSequenceDescriptorPath()
				.equals(Melody.getContext().getProcessorManager()
						.getSequenceDescriptor().getSourceFile())) {
			// launch the order
			Element elmt = Order.findOrder(getOrderName(), Melody.getContext()
					.getProcessorManager().getSequenceDescriptor());
			try {
				Melody.getContext().processTask(elmt);
			} catch (TaskException Ex) {
				throw new SourceException(Ex);
			}
		} else {
			IProcessorManager pm = Melody.getContext()
					.createSubProcessorManager();
			try {
				pm.getSequenceDescriptor()
						.load(csb.getSequenceDescriptorPath());
				pm.getSequenceDescriptor().addOrder(csb.getOrderName());
				pm.startProcessingSync();
			} catch (IOException Ex) {
				throw new SourceException(Ex);
			} catch (IllegalDocException | IllegalFileException
					| IllegalOrderException
					| ProcessorManagerConfigurationException Ex) {
				throw new RuntimeException("shouldn't happened.", Ex);
			}
			Throwable ex = pm.getProcessingFinalError();
			if (ex != null) {
				throw new SourceException(ex);
			}
		}
	}

	public OrderName getOrderName() {
		return _orderName;
	}

	/*
	 * Because it is necessary to the job, this attribute should be mandatory.
	 * But when created in SourceShortcut context, this attribute will never be
	 * declared. So it can't be annotated as mandatory.
	 */
	@Attribute(name = ORDER_ATTR)
	public OrderName setOrderName(OrderName name) {
		OrderName previous = getOrderName();
		_orderName = name;
		return previous;
	}

	@NestedElement(name = PARAM, type = NestedElement.Type.ADD)
	public void addParam(Property p) {
		if (p == null) {
			throw new IllegalArgumentException(
					"null: Not accepted. " + "Must be a valid "
							+ Property.class.getCanonicalName() + ".");
		}
		Melody.getContext().getProperties().put(p);
	}

}