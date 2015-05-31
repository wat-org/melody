package com.wat.melody.api;

import org.w3c.dom.Element;

import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.PropertySet;

/**
 * <p>
 * Maintains the list of registered {@link ITask} {@link Class}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IRegisteredTasks {

	/**
	 * <p>
	 * Register the given Java {@link @Class} as a {@link ITask}.
	 * </p>
	 * 
	 * @param c
	 *            is the {@link ITask} {@link Class} to register.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Class} is <code>null</code> .
	 * @throws RuntimeException
	 *             if the given object is not a valid {@link ITask}
	 *             {@link Class} (ex : not public, abstract, ...).
	 */
	public void registerJavaTask(Class<? extends ITask> c);

	/**
	 * <p>
	 * Register the given Call Shortcut (a sequence descriptor and an order in
	 * it) as a {@link ITask}.
	 * </p>
	 * 
	 * @param order
	 *            is the order to register.
	 * @param condition
	 *            is the condition when the given order is eligible.
	 * @param sequenceDescriptor
	 *            is the sequence descriptor, which contains the given order.
	 * 
	 * @throws IllegalArgumentException
	 *             if any of the given values is <code>null</code> .
	 * @throws RuntimeException
	 *             if the given object is not a valid {@link ITask}
	 *             {@link Class} (ex : not public, abstract, ...).
	 */
	public void registerCallShortcutTask(OrderName order,
			ISequenceDescriptor sequenceDescriptor, ICondition condition);

	/**
	 * @param taskName
	 *            is the name of the {@link ITask} to find.
	 * @param elmt
	 *            is the {@link Element} which contains the task in its native
	 *            format.
	 * @param ps
	 *            is the {@link PropertySet} which contains the task context.
	 * 
	 * @return an {@link ITaskBuilder}, which can create the {@link ITask} which
	 *         have been registered under the given name, and whose conditions
	 *         matches the given context, or <code>null</code> if no
	 *         {@link ITask} have been registered with such name or if the
	 *         conditions doesn't meet.
	 * 
	 * @throws IllegalArgumentException
	 *             if of the given values is <code>null</code> .
	 */
	public ITaskBuilder retrieveEligibleTaskBuilder(String taskName,
			Element elmt, PropertySet ps);

}