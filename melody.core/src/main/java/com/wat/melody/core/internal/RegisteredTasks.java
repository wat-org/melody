package com.wat.melody.core.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.Element;

import com.wat.melody.api.ICondition;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.core.internal.taskbuilder.CallShortcutBuilder;
import com.wat.melody.core.internal.taskbuilder.JavaTaskBuilder;
import com.wat.melody.core.nativeplugin.attributes.RemoveAttribute;
import com.wat.melody.core.nativeplugin.attributes.SetAttributeValue;
import com.wat.melody.core.nativeplugin.call.Call;
import com.wat.melody.core.nativeplugin.foreach.Foreach;
import com.wat.melody.core.nativeplugin.order.Order;
import com.wat.melody.core.nativeplugin.property.Property;
import com.wat.melody.core.nativeplugin.sequence.Sequence;
import com.wat.melody.core.nativeplugin.synchronize.Synchronize;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RegisteredTasks extends Hashtable<String, List<ITaskBuilder>>
		implements IRegisteredTasks {

	private static final long serialVersionUID = 3001756548734600804L;

	/**
	 * <p>
	 * Tests if the given subject {@link Class} is a valid {@link ITask}.
	 * </p>
	 * 
	 * @param c
	 *            is the subject {@link Class} to test.
	 * 
	 * @return <tt>true</tt> if the given subject {@link Class} is a valid
	 *         {@link ITask}, or <tt>false</tt> if not.
	 */
	public static boolean isValidTask(Class<? extends ITask> c) {
		if (!Modifier.isPublic(c.getModifiers())
				|| Modifier.isAbstract(c.getModifiers())) {
			return false;
		}
		for (Constructor<?> ctor : c.getConstructors()) {
			// If the Class have a public no-argument constructor
			if (ctor.getParameterTypes().length == 0
					&& Modifier.isPublic(ctor.getModifiers())
					&& !Modifier.isAbstract(ctor.getModifiers())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Contain all native Task Java Classes : {@link Sequence}, {@link Order},
	 * {@link Call}, {@link Foreach}, {@link Property},
	 * {@link SetAttributeValue}, {@link RemoveAttribute}.
	 * </p>
	 */
	public RegisteredTasks() {
		registerJavaTask(Sequence.class);
		registerJavaTask(Order.class);
		registerJavaTask(Call.class);
		registerJavaTask(Foreach.class);
		registerJavaTask(Synchronize.class);
		registerJavaTask(Property.class);
		registerJavaTask(SetAttributeValue.class);
		registerJavaTask(RemoveAttribute.class);
	}

	@Override
	public void registerJavaTask(Class<? extends ITask> c) {
		register(new JavaTaskBuilder(c));
	}

	@Override
	public void registerCallShortcutTask(OrderName order,
			ISequenceDescriptor sequenceDescriptor, ICondition condition) {
		register(new CallShortcutBuilder(order, sequenceDescriptor, condition));
	}

	public void register(ITaskBuilder tb) {
		if (tb == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid "
					+ ITaskBuilder.class.getCanonicalName() + ".");
		}
		Class<? extends ITask> c = tb.getTaskClass();
		if (!isValidTask(c)) {
			throw new RuntimeException("The given Java Class is not a "
					+ "valid " + ITask.class.getCanonicalName()
					+ ". This Java Class is either not public, or abstract "
					+ "or it as no 0-arg constructor.");
		}
		String taskName = c.getSimpleName().toLowerCase();
		Task a = c.getAnnotation(Task.class);
		if (a != null) {
			// declare an entry TaskAnnotaionName->canonicalClassName
			taskName = a.name().toLowerCase();
		}

		List<ITaskBuilder> tbs = get(taskName);
		// initialize the ITaskBuilder List if needed
		if (tbs == null) {
			tbs = new ArrayList<ITaskBuilder>();
			super.put(taskName, tbs);
		}
		// add the given ItaskBuilder at the end of the list
		tbs.add(tb);
	}

	public List<ITaskBuilder> get(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Task Name).");
		}
		return super.get(taskName.toLowerCase());
	}

	@Override
	public ITaskBuilder retrieveEligibleTaskBuilder(String taskName,
			Element elmt, PropertySet ps) {
		if (elmt == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ " (a Task Name).");
		}
		if (ps == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ " (a Task Name).");
		}
		List<ITaskBuilder> tbs = get(taskName);
		if (tbs == null) {
			return null;
		}
		for (ITaskBuilder tb : tbs) {
			if (tb.isEligible(elmt, ps)) {
				return tb;
			}
		}
		// should never go here
		return null;
	}

}