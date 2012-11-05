package com.wat.melody.core.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ITask;
import com.wat.melody.core.nativeplugin.call.Call;
import com.wat.melody.core.nativeplugin.foreach.Foreach;
import com.wat.melody.core.nativeplugin.order.Order;
import com.wat.melody.core.nativeplugin.property.Property;
import com.wat.melody.core.nativeplugin.sequence.Sequence;
import com.wat.melody.core.nativeplugin.setEDAttrValue.SetEDAttrValue;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RegisteredTasks extends Hashtable<String, Class<ITask>> implements
		IRegisteredTasks {

	private static final long serialVersionUID = 3001756548734600804L;

	/**
	 * <p>
	 * Tests if the given subject class is a valid {#link {@link ITask}.
	 * </p>
	 * 
	 * @param c
	 *            is the subject class.
	 * 
	 * @return <code>true</code> if the given subject class is a valid {#link
	 *         {@link ITask}, or <code>false</code> if not.
	 */
	public static boolean isValidTask(Class<ITask> c) {
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
	 * This new object contain all native Task Java Classes : {@link Sequence},
	 * {@link Order}, {@link Call}, {@link Foreach}.
	 * </p>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public RegisteredTasks() {
		registerTaskClass((Class<ITask>[]) new Class<?>[] { Sequence.class,
				Order.class, Call.class, Foreach.class, SetEDAttrValue.class,
				Property.class });
	}

	/**
	 * <p>
	 * Registers all the given Task Java Classes.
	 * </p>
	 * 
	 * @param cs
	 *            is the Task Java Classes to register.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Task Java Classes to register or if one of the
	 *             Task Java Class to register is <code>null</code>.
	 */
	public void registerTaskClass(Class<ITask>[] cs) {
		if (cs == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid Class<ITask>[].");
		}
		for (Class<ITask> c : cs) {
			registerTaskClass(c);
		}
	}

	/**
	 * <p>
	 * Registers the given Task Java Class.
	 * </p>
	 * 
	 * @param c
	 *            is the Task Java Class to register.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Task Java Class to register is <code>null</code>
	 *             .
	 */
	@Override
	public Class<ITask> registerTaskClass(Class<ITask> c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid Class<ITask>.");
		}
		if (!isValidTask(c)) {
			throw new RuntimeException("The given Task Java Class is not a "
					+ "valid ITask. This class is either not public, abstract "
					+ "or it as no 0-arg constructor. ");
		}
		return put(c.getSimpleName().toLowerCase(), c);
	}

	/**
	 * <p>
	 * Get the Task Java Class whose name match the given Name.
	 * </p>
	 * 
	 * @param taskName
	 *            is the name of the Task Java Class to find.
	 * 
	 * @return the Task Java Class whose name match the given Name, or
	 *         <code>null</code> if no Task Java Class have been registered with
	 *         such Name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Name is <code>null</code> .
	 */
	@Override
	public Class<ITask> getRegisteredTaskClass(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid String (a Task Name).");
		}
		return get(taskName.toLowerCase());
	}

	/**
	 * <p>
	 * Test weather the Task Java Class whose name match the given Name have
	 * been registered.
	 * </p>
	 * 
	 * @param taskName
	 *            is the name of the Task Java Class to find.
	 * 
	 * @return <code>true</code> if a Task Java Class whose name match the given
	 *         Name have been registered, <code>false</code> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Name is <code>null</code> .
	 */
	@Override
	public boolean containsRegisteredTaskClass(String taskName) {
		if (taskName == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid String (a Task Name).");
		}
		return containsKey(taskName.toLowerCase());
	}
}
