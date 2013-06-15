package com.wat.melody.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * Stop the associated {@link IProcessorManager} on JVM termination.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ShutdownHook extends Thread {

	private static Log log = LogFactory.getLog(ShutdownHook.class);

	private IProcessorManager _processorManager;
	private Thread _launcherThread;

	/**
	 * <p>
	 * Create a new {@link ShutdownHook}, which will, on JVM termination,
	 * properly stop the given {@link IProcessorManager} and wait for the given
	 * {@link Thread} to end.
	 * </p>
	 * 
	 * @param pm
	 *            is the {@link IProcessorManager} to stop.
	 * @param t
	 *            is the Launcher {@link Thread} to wait.
	 */
	public ShutdownHook(IProcessorManager pm, Thread t) {
		setProcessorManager(pm);
		setLauncherThread(t);
		Runtime.getRuntime().addShutdownHook(this);
	}

	/**
	 * <p>
	 * Will be called on JVM termination (i.e. call to System.exit, CTRL-C
	 * trapped, ...).
	 * </p>
	 * 
	 * <ul>
	 * <li>Will stop the associated {@link IProcessorManager} and wait for the
	 * processing to be done ;</li>
	 * <li>Will stop the launcher thread and wait for its end ;</li>
	 * </ul>
	 */
	public void run() {
		try {
			log.info("Exit sequence engaged ...");

			// Request Engine to stop
			getProcessorManager().stopProcessing();
			// Wait till the Engine's work is done
			getProcessorManager().waitTillProcessingIsDone();

			// Request the Launcher Thread to stop
			getLauncherThread().interrupt();
			// Wait till the Launcher Thread's work is done
			getLauncherThread().join();
		} catch (Throwable Ex) {
			throw new RuntimeException("Unexecpted error while shutting down.",
					Ex);
		}
	}

	private IProcessorManager getProcessorManager() {
		return _processorManager;
	}

	private IProcessorManager setProcessorManager(IProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IProcessorManager.class.getCanonicalName() + ".");
		}
		return _processorManager = pm;
	}

	private Thread getLauncherThread() {
		return _launcherThread;
	}

	private Thread setLauncherThread(Thread t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Thread.class.getCanonicalName()
					+ ".");
		}
		return _launcherThread = t;
	}

}