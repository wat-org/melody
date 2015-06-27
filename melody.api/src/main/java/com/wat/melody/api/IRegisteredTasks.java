package com.wat.melody.api;

import java.io.IOException;

import org.w3c.dom.Element;

import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.order.exception.IllegalOrderNameException;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.exception.IllegalDocException;

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
	 * Register the given extension.
	 * </p>
	 * 
	 * @param extensionPath
	 *            is the path of the Extension to register.
	 * @param defaultSequenceDescriptorPath
	 *            is the path of the Sequence Descriptor to load if the given
	 *            Extension path does not contains extension's descriptor. Can
	 *            be <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given extension path is <code>null</code> .
	 */
	public void registerExtension(String extensionPath,
			String defaultSequenceDescriptorPath) throws IllegalDocException,
			IllegalFileException, IllegalOrderException,
			IllegalOrderNameException, IOException;

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