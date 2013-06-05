package com.wat.melody.core.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ITask;
import com.wat.melody.api.annotation.Task;
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
public class RegisteredTasks extends Hashtable<String, Class<? extends ITask>>
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
		put(Sequence.class);
		put(Order.class);
		put(Call.class);
		put(Foreach.class);
		put(Synchronize.class);
		put(Property.class);
		put(SetAttributeValue.class);
		put(RemoveAttribute.class);
	}

	@Override
	public Class<? extends ITask> put(Class<? extends ITask> c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + "<"
					+ ITask.class.getCanonicalName() + ">.");
		}
		if (!isValidTask(c)) {
			throw new RuntimeException("The given Java Class is not a "
					+ "valid " + ITask.class.getCanonicalName()
					+ ". This Java Class is either not public, or abstract "
					+ "or it as no 0-arg constructor. ");
		}
		Task a = c.getAnnotation(Task.class);
		if (a != null) {
			// declare an entry TaskAnnotaionName->canonicalCalssName
			super.put(a.name().toLowerCase(), c);
		}
		// declare an entry className->canonicalCalssName
		return super.put(c.getSimpleName().toLowerCase(), c);
	}

	@Override
	public Class<? extends ITask> get(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Task Name).");
		}
		return super.get(taskName.toLowerCase());
	}

	@Override
	public boolean contains(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Task Name).");
		}
		return super.containsKey(taskName.toLowerCase());
	}
}
