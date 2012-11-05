package com.wat.melody.cli;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * Stop the associated <code>IProcessorManager</code> on JVM termination.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ShutdownHook extends Thread {

	private IProcessorManager moProcessorManager;
	private Thread moLauncherThread;

	/**
	 * <p>
	 * Create a new <code>ShutdownHook</code>, which will, on JVM termination,
	 * properly stop the given <code>IProcessorManager</code> and wait for the
	 * given <code>Thread</code> to end.
	 * </p>
	 * 
	 * @param pm
	 *            is the <code>IProcessorManager</code> to stop.
	 * @param t
	 *            is the Launcher Thread to wait.
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
	 * <p>
	 * <i> * Will stop the associated <code>IProcessorManager</code> and wait
	 * for the processing to be done. <BR/>
	 * * Will stop the launcher thread to wait for its end. <BR/>
	 * </i>
	 * </p>
	 */
	public void run() {
		try {
			System.out.println("Exit sequence engaged ...");

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
		return moProcessorManager;
	}

	private IProcessorManager setProcessorManager(IProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid IProcessorManager.");
		}
		return moProcessorManager = pm;
	}

	private Thread getLauncherThread() {
		return moLauncherThread;
	}

	private Thread setLauncherThread(Thread t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Thread.");
		}
		return moLauncherThread = t;
	}

}