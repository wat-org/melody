package com.wat.melody.cli;

import java.io.IOException;
import java.lang.Thread.State;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.utils.ReturnCode;

public class Launcher {

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

		ProcessorManagerLoader pml = null;
		IProcessorManager pm = null;

		// Before the logger is created, log into stderr
		try {
			pml = new ProcessorManagerLoader();
			pm = pml.parseCommandLine(args);
		} catch (MelodyException Ex) {
			System.err.println(Util.getUserFriendlyStackTrace(Ex));
			System.exit(ReturnCode.KO.getValue());
		} catch (Throwable Ex) {
			Exception e = new Exception("Something bad happend. "
					+ "Please report this bug at Wat-Org.", Ex);
			System.err.println(Util.getUserFriendlyStackTrace(e));
			System.exit(ReturnCode.ERRGEN.getValue());
		}

		// The logger can only be created after log4j's initialization
		Log log = LogFactory.getLog(Launcher.class);
		ShutdownHook sdh = null;
		DefaultProcessingListener dpl = null;
		ReturnCode iReturnCode = ReturnCode.ERRGEN;

		try {
			sdh = new ShutdownHook(pm, Thread.currentThread());
			dpl = new DefaultProcessingListener(pm);
			pm.startProcessing();
			pm.waitTillProcessingIsDone();
			if (pm.getProcessingFinalError() != null) {
				throw pm.getProcessingFinalError();
			}
			iReturnCode = ReturnCode.OK;
		} catch (InterruptedException Ex) {
			log.warn(Util.getUserFriendlyStackTrace(Ex));
			iReturnCode = ReturnCode.INTERRUPTED;
		} catch (MelodyException Ex) {
			log.error(Util.getUserFriendlyStackTrace(Ex));
			iReturnCode = ReturnCode.KO;
		} catch (Throwable Ex) {
			Exception e = new Exception("Something bad happend. "
					+ "Please report this bug at Wat-Org.", Ex);
			log.fatal(Util.getUserFriendlyStackTrace(e));
			iReturnCode = ReturnCode.ERRGEN;
		} finally {
			if (pm != null && dpl != null) {
				pm.removeListener(dpl);
			}
			try {
				pml.deleteTemporaryResources();
			} catch (IOException Ex) {
				Exception e = new Exception("Failed to delete CLI temporary "
						+ "resources.", Ex);
				log.warn(Util.getUserFriendlyStackTrace(e));
			}
			// If we call System.exit while the processing has already been
			// stopped by user (i.e. the shutdownHook is started), it will block
			// indefinitely. (See Runtine.getRuntime().exit)
			// This test is able to detect if the shutdownHook is running or
			// not.
			if (sdh == null || sdh.getState() == State.NEW) {
				if (sdh != null) {
					Runtime.getRuntime().removeShutdownHook(sdh);
				}
				System.exit(iReturnCode.getValue());
			}
		}
	}

}