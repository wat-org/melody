package com.wat.melody.api;

/**
 * <p>
 * Maintains the list of registered {@link IPlugInConfiguration} objects.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IPlugInConfigurations {

	/**
	 * <p>
	 * Registers the given {@link IPlugInConfiguration}.
	 * </p>
	 * 
	 * @param c
	 *            is the {@link IPlugInConfiguration} to register.
	 * 
	 * @return the previously registered {@link IPlugInConfiguration}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Class} is <code>null</code> .
	 */
	public IPlugInConfiguration put(IPlugInConfiguration conf);

	/**
	 * <p>
	 * Get the registered {@link IPlugInConfiguration} whose name match the
	 * given name.
	 * </p>
	 * 
	 * @param name
	 *            is the name of the {@link IPlugInConfiguration} to find.
	 * 
	 * @return the {@link IPlugInConfiguration} whose name match the given name,
	 *         or <code>null</code> if no {@link IPlugInConfiguration} have been
	 *         registered with such name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code> .
	 */
	public IPlugInConfiguration get(Class<? extends IPlugInConfiguration> name);

	/**
	 * <p>
	 * Test weather a {@link IPlugInConfiguration} whose name match the given
	 * name have been registered.
	 * </p>
	 * 
	 * @param name
	 *            is the name of the {@link IPlugInConfiguration} to find.
	 * 
	 * @return <code>true</code> if a {@link IPlugInConfiguration} whose name
	 *         match the given name have been registered, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code> .
	 */
	public boolean contains(Class<? extends IPlugInConfiguration> name);

}