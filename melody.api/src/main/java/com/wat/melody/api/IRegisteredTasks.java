package com.wat.melody.api;

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
	 * Registers the given {@link Class} as an {@link ITask}.
	 * </p>
	 * 
	 * @param c
	 *            is the {@link Class} to register.
	 * 
	 * @return the previously registered {@link ITask} {@link Class}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Class} is <code>null</code> .
	 * @throws RuntimeException
	 *             if the given {@link Class} is not a valid {@link ITask}
	 *             {@link Class} (ex : not public, abstract, ...).
	 */
	public Class<? extends ITask> put(Class<? extends ITask> c);

	/**
	 * <p>
	 * Get the registered {@link ITask} {@link Class} whose name match the given
	 * name.
	 * </p>
	 * 
	 * @param taskName
	 *            is the name of the {@link ITask} {@link Class} to find.
	 * 
	 * @return the {@link ITask} {@link Class} whose name match the given name,
	 *         or <code>null</code> if no {@link ITask} {@link Class} have been
	 *         registered with such name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code> .
	 */
	public Class<? extends ITask> get(String taskName);

	/**
	 * <p>
	 * Test weather a {@link ITask} {@link Class} whose name match the given
	 * name have been registered.
	 * </p>
	 * 
	 * @param taskName
	 *            is the name of the {@link ITask} {@link Class} to find.
	 * 
	 * @return <code>true</code> if a {@link ITask} {@link Class} whose name
	 *         match the given name have been registered, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code> .
	 */
	public boolean contains(String taskName);

}
