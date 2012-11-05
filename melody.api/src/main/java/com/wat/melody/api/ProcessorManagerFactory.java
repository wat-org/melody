package com.wat.melody.api;

import com.wat.melody.api.exception.ProcessorManagerFactoryException;

/**
 * <p>
 * Creates {@link IProcessorManager} objects.
 * </p>
 * 
 * <p>
 * The system property {@link #PROCESSOR_MANAGER_IMPL_KEY} must be equal to the
 * canonical class name of a {@link IProcessorManager} implementation. Use
 * {@link System#setProperty(String, String)} to set such property.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessorManagerFactory {

	public static final String PROCESSOR_MANAGER_IMPL_KEY = "com.wat.melody.ProcessorManagerImpl.canonicalClassName";

	private Class<IProcessorManager> moPMClass;

	public static ProcessorManagerFactory newInstance()
			throws ProcessorManagerFactoryException {
		return new ProcessorManagerFactory();
	}

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
	}
}