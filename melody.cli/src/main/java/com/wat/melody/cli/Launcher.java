package com.wat.melody.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.utils.ReturnCode;

public class Launcher {

	static {
		// will redirect (e.g. bridge) JUL (java.util.logging) to SLF4J
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	/**
	 * Launch the processing sequence. First, parse the command line, then fill
	 * the GlobalConfiguration with values extracted from the command line or
	 * taken in the Global Configuration File. Then, launch the processing
	 * sequence.
	 * 
	 * @param args
	 *            is a string array, which should match the specifications of
	 *            {@link ProcessorManagerLoader#parseCommandLine(String[])}.
	 * 
	 * @return nothing but exit (i.e. call to System.exit) with 0 if succeed, 1
	 *         if failed, 130 if interrupted, or any other value (from 2 to 255
	 *         - 130) if an unexpected error occurred.
	 */
	public static void main(String[] args) {

		ProcessorManagerLoader pml = new ProcessorManagerLoader();
		IProcessorManager pm = null;

		// Before the logger is created, log into stderr
		try {
			pm = pml.parseCommandLine(args);
		} catch (MelodyException Ex) {
			System.err.println(Ex.getUserFriendlyStackTrace());
			System.exit(ReturnCode.KO.getValue());
		} catch (Throwable Ex) {
			MelodyException e = new MelodyException("Something bad happend. "
					+ "Please report this bug at Wat-Org.", Ex);
			System.err.println(e.getUserFriendlyStackTrace());
			System.exit(ReturnCode.ERRGEN.getValue());
		}

		// The logger can only be created after log4j's initialization
		Logger log = LoggerFactory.getLogger(Launcher.class);
		Logger ex = LoggerFactory.getLogger("exception."
				+ Launcher.class.getName());
		ShutdownHook sdh = new ShutdownHook(pm, Thread.currentThread());
		// DefaultProcessingListener dpl = new DefaultProcessingListener(pm);
		ReturnCode iReturnCode = ReturnCode.ERRGEN;

		try {
			pm.startProcessing();
			pm.waitTillProcessingIsDone();
			if (pm.getProcessingFinalError() != null) {
				throw pm.getProcessingFinalError();
			}
			iReturnCode = ReturnCode.OK;
		} catch (InterruptedException Ex) {
			MelodyException e = new MelodyException(Ex);
			log.warn(e.getUserFriendlyStackTrace());
			ex.warn(e.getFullStackTrace());
			iReturnCode = ReturnCode.INTERRUPTED;
		} catch (MelodyException Ex) {
			log.error(Ex.getUserFriendlyStackTrace());
			ex.error(Ex.getFullStackTrace());
			iReturnCode = ReturnCode.KO;
		} catch (Throwable Ex) {
			MelodyException e = new MelodyException("Something bad happend. "
					+ "Please report this bug at Wat-Org.", Ex);
			log.error(e.getUserFriendlyStackTrace());
			ex.error(e.getFullStackTrace());
			iReturnCode = ReturnCode.ERRGEN;
		} finally {
			// if (pm != null && dpl != null) {
			// pm.removeListener(dpl);
			// }
			try {
				pml.deleteTemporaryResources();
			} catch (IOException Ex) {
				MelodyException e = new MelodyException("Fail to delete CLI "
						+ "temporary resources.", Ex);
				log.warn(e.getUserFriendlyStackTrace());
				ex.warn(e.getFullStackTrace());
			}
			// If we call System.exit while the processing has already been
			// stopped by user (i.e. the shutdownHook is started), it will block
			// indefinitely. (See Runtine.getRuntime().exit)
			// removeShutdownHook will throw IllegalStateException if the
			// shutdownHook is running.
			try {
				Runtime.getRuntime().removeShutdownHook(sdh);
				System.exit(iReturnCode.getValue());
			} catch (IllegalStateException ignored) {
			}
		}
	}

}