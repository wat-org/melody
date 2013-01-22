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
	 *            : a string array, respecting the following pattern</BR>
	 *            [Default Global Configuration File Path] [options]+</BR> </BR>
	 *            [Default Global Configuration File Path] : is the path of the
	 *            Default Global Configuration File.</BR> The Default Global
	 *            Configuration File will be loaded only if the Global
	 *            Configuration File Path is not provided in the command line
	 *            (see option -C).</BR> </BR> [options] :</BR> -C [Global
	 *            Configuration File Path] : the path of the Global
	 *            Configuration File to load.</BR> If not provided in the
	 *            command line, the Default Global Configuration file (given by
	 *            args[0]) will be loaded.</BR> This Global Configuration File
	 *            must contains the following Configuration Directives :</BR>
	 *            workingFolderPath : the path of the directory where all
	 *            temporary files will be generated</BR>
	 *            extractedPackageFolderPath : the path of the directory where
	 *            the given Package (if provided) will be extracted</BR>
	 *            archivedLogsFolderPath : the path of the directory which
	 *            contains all archived logs</BR> logsFolderPath : the path of
	 *            the directory which will contains all logs</BR>
	 *            pluginConfigurationFolderPath : the path of the directory
	 *            where the plugin configuration is stored</BR>
	 *            maxSimultaneousStep : the number of simultaneous step (not
	 *            used yet)</BR> hardKillTimeout : the number of second the
	 *            engine will wait before kill (not used yet)</BR> </BR> -qqq :
	 *            quiet mode : only display FATAL.</BR> -qq : quiet mode : only
	 *            display CRITICAL.</BR> -q : quiet mode : only display
	 *            ERROR.</BR> -v : verbose mode : display INFO and higher.</BR>
	 *            -vv : very verbose mode : display DEBUG and higher.</BR> -vvv
	 *            : very very verbose : display TRACE and higher.</BR> If not
	 *            provided in the command line, the parameter 'logThreshold',
	 *            defined in the Global Configuration File, will be used.</BR>
	 *            If not provided in the command line and not defined in the
	 *            Global Configuration File, will be set to WARNING.</BR> </BR>
	 *            -E [Resources Descriptor File path] : the path of the
	 *            Resources Descriptor File.</BR> If not provided in the command
	 *            line, the parameter 'resourcesDescriptorFilePath', defined in
	 *            the Global configuration file, will be used.</BR> If not
	 *            provided in the command line and not defined in the Global
	 *            Configuration File, will raise an exception.</BR> NB : the
	 *            'Resources Descriptor File' is a sole XML file which must fit
	 *            the 'Resources Descriptor Specifications'.</BR> </BR> -f
	 *            [Sequence Descriptor File path] : the path of the Sequence
	 *            Descriptor File.</BR> If not provided in the command line, the
	 *            parameter 'sequenceDescriptorFilePath', defined in the Global
	 *            configuration file, will be used.</BR> If not provided in the
	 *            command line and not defined in the Global Configuration File,
	 *            will raise an exception.</BR> NB : the 'Sequence Descriptor
	 *            File' can either be a sole XML file or a package. In the case
	 *            of a sole XML file, it must fit the 'Sequence Descriptor
	 *            Specifications'. In the case it is a package, it must fit the
	 *            'Package Specifications'.</BR> </BR> -F [Filter] : will reduce
	 *            the Resources Descriptor to all matching elements.</BR> If not
	 *            provided in the command line, the full content of the
	 *            Resources Descriptor is keep.</BR> If set multiple times in
	 *            the command line, it will reduce and reduce and reduce the
	 *            Resources Descriptor.</BR> NB : the 'Filter' is a XPath 2.0
	 *            Expression.</BR> </BR> -T [Filter] : will reduce the Resources
	 *            Descriptor to all matching elements.</BR> If not provided in
	 *            the command line, the full content of the Resources Descriptor
	 *            is keep.</BR> If set multiple times in the command line, it
	 *            will reduce and reduce and reduce the Resources
	 *            Descriptor.</BR> NB : the 'Filter' is a XPath 2.0
	 *            Expression.</BR> </BR> -o [order] : The sequence will start at
	 *            the order [order].</BR> If not provided in the command line,
	 *            the parameter 'order', defined in the Global configuration
	 *            file, will be used.</BR> If not provided in the command line
	 *            and not defined in the Global Configuration File, the sequence
	 *            will start at the 'Default Order'.</BR> NB : the 'Default
	 *            Order' is either the sole 'order' XML element defined in the
	 *            Sequence Descriptor or the 'order' XML element which have the
	 *            'isDefault' XML attribute set to 'TRUE', 'Y', 'YES' or
	 *            '1'.</BR> NB : the 'Order' must match an Order defined in the
	 *            given Sequence Descriptor.</BR> </BR> -s [step] : must match a
	 *            step defined in the sequence descriptor of the given package.
	 *            The sequence will start at the given order (see option -o) and
	 *            will only execute the given step.</BR> If not provided in the
	 *            command line, the parameter 'step', defined in the Global
	 *            configuration file, will be used.</BR> If not provided in the
	 *            command line and not defined in the Global Configuration File,
	 *            the sequence will start at the given order and will execute
	 *            all steps.</BR> NB : the 'Step' must match an Step defined in
	 *            the given Sequence Descriptor.</BR> </BR> -S [specific
	 *            configuration file path]> : some XPath expression defined in
	 *            the given package requires an additional XML file.</BR> If not
	 *            provided in the command line, the parameter
	 *            'specificConfigurationFilePath', defined in the Global
	 *            configuration file, will be used.</BR> If not provided in the
	 *            command line and not defined in the Global Configuration File,
	 *            and if some XPath Expression defined in the given package
	 *            requires it, an error will be raised.</BR> NB : the 'Specific
	 *            Configuration File' is an XML file which must fit the
	 *            'Specific Configuration Specifications'.</BR> </BR> -B :
	 *            enable 'Batch Mode' : run the tool in batch mode. Will answer
	 *            'YES' to all question.</BR> -b : disable 'Batch Mode'.</BR> If
	 *            not provided in the command line, the parameter 'batchMode',
	 *            defined in the Global configuration file, will be used.</BR>
	 *            </BR> -P : enable 'Preserve Temporary Files Mode' : will
	 *            preserve temporary files for deletion (useful when
	 *            debugging).</BR> -p : disable 'Preserve Temporary Files
	 *            Mode'.</BR> If not provided in the command line, the parameter
	 *            'preserveTemporaryFilesMode', defined in the Global
	 *            configuration file, will be used.</BR> </BR> -D : enable 'Run
	 *            Dry Mode' : will not execute anything. Used to validate all
	 *            input data before real execution.</BR> -d : disable 'Run Dry
	 *            Mode'.</BR> If not provided in the command line, the parameter
	 *            'runDryMode', defined in the Global configuration file, will
	 *            be used.</BR>
	 * 
	 * @return nothing but exit (i.e. call to System.exit) with 0 if succeed, 1
	 *         if failed or any other value (from 2 to 255) if an unexpected
	 *         error occurred.
	 */
	public static void main(String[] args) {

		ProcessorManagerLoader pml = null;
		IProcessorManager pm = null;

		try {
			pml = new ProcessorManagerLoader();
			pm = pml.parseCommandLine(args);
		} catch (MelodyException Ex) {
			System.out.println(Util.getUserFriendlyStackTrace(Ex));
			System.exit(ReturnCode.KO.getValue());
		} catch (Throwable Ex) {
			System.out
					.println("Something bad happend. Please report this bug at Wat-Org."
							+ Util.NEW_LINE
							+ Util.getUserFriendlyStackTrace(Ex));
			System.exit(ReturnCode.ERRGEN.getValue());
		}

		// The logger is created after log4j's initialization
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
		} catch (MelodyException Ex) {
			log.error(Util.getUserFriendlyStackTrace(Ex));
			iReturnCode = ReturnCode.KO;
		} catch (InterruptedException Ex) {
			log.warn(Util.getUserFriendlyStackTrace(Ex));
			iReturnCode = ReturnCode.INTERRUPTED;
		} catch (Throwable Ex) {
			Exception e = new Exception("Something bad happend. "
					+ "Please report this bug at Wat-Org.", Ex);
			log.fatal(Util.getUserFriendlyStackTrace(e));
			iReturnCode = ReturnCode.ERRGEN;
		} finally {
			if (pm != null && dpl != null)
				pm.removeListener(dpl);
			if (pml != null)
				try {
					pml.deleteTemporaryResources();
				} catch (IOException Ex) {
					Exception e = new Exception("Failed to delete CLI "
							+ "temporary resources.", Ex);
					log.error(Util.getUserFriendlyStackTrace(e));
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