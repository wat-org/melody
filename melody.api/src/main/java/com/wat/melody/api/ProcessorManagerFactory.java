package com.wat.melody.api;

import com.wat.melody.api.exception.ProcessorManagerFactoryException;

/**
 * <p>
 * This factory have the capacity to create new {@link IProcessorManager}.
 * </p>
 * <p>
 * The concrete implementation of the {@link IProcessorManager} this factory
 * will create is defined in the system property
 * {@link #PROCESSOR_MANAGER_IMPL_KEY}.
 * </p>
 * <p>
 * The system property {@link #PROCESSOR_MANAGER_IMPL_KEY} must be equal to the
 * canonical class name of an {@link IProcessorManager} implementation. Use
 * {@link System#setProperty(String, String)} to set such property.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessorManagerFactory {

	public static final String PROCESSOR_MANAGER_IMPL_KEY = "com.wat.melody.ProcessorManagerImpl.canonicalClassName";

	private Class<IProcessorManager> moPMClass;

	/**
	 * <p>
	 * Create a new {@link ProcessorManagerFactory}, which have the capacity to
	 * create new {@link IProcessorManager}.
	 * </p>
	 * <p>
	 * The concrete implementation of the {@link IProcessorManager} this factory
	 * will create is defined in the system property
	 * {@link #PROCESSOR_MANAGER_IMPL_KEY}.
	 * </p>
	 * <p>
	 * The system property {@link #PROCESSOR_MANAGER_IMPL_KEY} must be equal to
	 * the canonical class name of an {@link IProcessorManager} implementation.
	 * Use {@link System#setProperty(String, String)} to set such property.
	 * </p>
	 * 
	 * @return a new {@link ProcessorManagerFactory}.
	 * 
	 * @throws ProcessorManagerFactoryException
	 *             if the given implementation canonical class name is not a
	 *             valid {@link IProcessorManager}.
	 */
	public static ProcessorManagerFactory newInstance()
			throws ProcessorManagerFactoryException {
		return new ProcessorManagerFactory();
	}

	/**
	 * <p>
	 * Create a new {@link IProcessorManager}.
	 * </p>
	 * 
	 * @return a new {@link IProcessorManager}.
	 */
	public IProcessorManager newProcessorManager() {
		try {
			return moPMClass.newInstance();
		} catch (InstantiationException | IllegalAccessException Ex) {
			throw new RuntimeException("Unexecpted error while creating a new "
					+ moPMClass.getCanonicalName() + " object. "
					+ "Since the validity of this no contructor have been "
					+ "previously validated, such error cannot happened. "
					+ "Source code has certainly been modified "
					+ "and a bug have been introduced.", Ex);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private ProcessorManagerFactory() throws ProcessorManagerFactoryException {
		String pmClassName = System.getProperty(PROCESSOR_MANAGER_IMPL_KEY);
		if (pmClassName == null) {
			throw new ProcessorManagerFactoryException(Messages.bind(
					Messages.PMFactoryEx_UNDEF_ENV, new Object[] {
							ProcessorManagerFactory.class.getSimpleName(),
							IProcessorManager.class.getCanonicalName(),
							PROCESSOR_MANAGER_IMPL_KEY }));
		}
		try {
			moPMClass = (Class<IProcessorManager>) Class.forName(pmClassName);
		} catch (ClassNotFoundException Ex) {
			throw new ProcessorManagerFactoryException(Messages.bind(
					Messages.PMFactoryEx_CLASS_NOT_FOUND, Ex.getMessage(),
					IProcessorManager.class.getCanonicalName()));
		} catch (NoClassDefFoundError Ex) {
			throw new ProcessorManagerFactoryException(Messages.bind(
					Messages.PMFactoryEx_NO_CLASS_DEF_FOUND, pmClassName, Ex
							.getMessage().replaceAll("/", ".")));
		}
		try {
			IProcessorManager pm = moPMClass.newInstance();
		} catch (InstantiationException | IllegalAccessException Ex) {
			throw new ProcessorManagerFactoryException(Messages.bind(
					Messages.PMFactoryEx_ILLEGAL_ACCESS,
					moPMClass.getCanonicalName(),
					IProcessorManager.class.getCanonicalName()));
		} catch (ClassCastException Ex) {
			throw new ProcessorManagerFactoryException(Messages.bind(
					Messages.PMFactoryEx_CLASS_CAST,
					moPMClass.getCanonicalName(),
					IProcessorManager.class.getCanonicalName()));
		}
		// TODO : catch InvocationTargetException
	}
}