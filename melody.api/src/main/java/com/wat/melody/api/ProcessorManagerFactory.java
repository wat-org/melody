package com.wat.melody.api;

import java.lang.reflect.InvocationTargetException;

import com.wat.melody.api.exception.ProcessorManagerFactoryException;
import com.wat.melody.common.messages.Msg;

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
			return moPMClass.getConstructor().newInstance();
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | ClassCastException
				| InvocationTargetException Ex) {
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
			throw new ProcessorManagerFactoryException(Msg.bind(
					Messages.PMFactoryEx_UNDEF_ENV,
					ProcessorManagerFactory.class.getSimpleName(),
					IProcessorManager.class.getCanonicalName(),
					PROCESSOR_MANAGER_IMPL_KEY));
		}
		try {
			moPMClass = (Class<IProcessorManager>) Class.forName(pmClassName);
		} catch (ClassNotFoundException Ex) {
			throw new ProcessorManagerFactoryException(Msg.bind(
					Messages.PMFactoryEx_CLASS_NOT_FOUND, Ex.getMessage(),
					IProcessorManager.class.getCanonicalName()));
		} catch (NoClassDefFoundError Ex) {
			throw new ProcessorManagerFactoryException(Msg.bind(
					Messages.PMFactoryEx_NO_CLASS_DEF_FOUND, pmClassName, Ex
							.getMessage().replaceAll("/", ".")));
		}
		try {
			IProcessorManager pm = moPMClass.getConstructor().newInstance();
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | ClassCastException Ex) {
			throw new ProcessorManagerFactoryException(Msg.bind(
					Messages.PMFactoryEx_INVALID_SPEC,
					moPMClass.getCanonicalName(),
					IProcessorManager.class.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new ProcessorManagerFactoryException(Msg.bind(
					Messages.PMFactoryEx_INTERNAL_ERROR,
					moPMClass.getCanonicalName(),
					IProcessorManager.class.getCanonicalName()), Ex.getCause());
		}
	}
}