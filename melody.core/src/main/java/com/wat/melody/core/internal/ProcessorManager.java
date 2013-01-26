package com.wat.melody.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IPlugInConfigurations;
import com.wat.melody.api.IProcessorListener;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Messages;
import com.wat.melody.api.event.ProcessingFinishedEvent;
import com.wat.melody.api.event.ProcessingStartedEvent;
import com.wat.melody.api.event.RequestProcessingToPauseEvent;
import com.wat.melody.api.event.RequestProcessingToResumeEvent;
import com.wat.melody.api.event.RequestProcessingToStartEvent;
import com.wat.melody.api.event.RequestProcessingToStopEvent;
import com.wat.melody.api.event.State;
import com.wat.melody.api.event.TaskCreatedEvent;
import com.wat.melody.api.event.TaskFinishedEvent;
import com.wat.melody.api.event.TaskStartedEvent;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ProcessorException;
import com.wat.melody.api.exception.ProcessorManagerConfigurationException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.core.nativeplugin.sequence.exception.SequenceException;

/**
 * <p>
 * Start, pause, resume and stop the processing, according to the given
 * instructions.
 * </p>
 * <p>
 * Processing instructions are described in a {@link SequenceDescriptor}.
 * </p>
 * <p>
 * Processing behavior can be configured using the meaningful setters of this
 * object.
 * </p>
 * 
 * <p>
 * <i> * The processing is performed by a dedicated thread, which is created in
 * its own ThreadGroup. <BR/>
 * * This ThreadGroup is a child of the calling thread's parent ThreadGroup,
 * meaning that if the calling thread's parent ThreadGroup is interrupted, the
 * processing dedicated thread will receive the interruption too. </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class ProcessorManager implements IProcessorManager, Runnable {

	private static Log log = LogFactory.getLog(ProcessorManager.class);

	private TaskFactory moTaskFactory;
	private String msWorkingFolderPath;
	private int miMaxSimultaneousStep;
	private int miHardKillTimeout;
	private boolean mbBatchMode;
	private boolean mbPreserveTemporayFilesMode;
	private boolean mbRunDryMode;
	private SequenceDescriptor moSequenceDescriptor;
	private ResourcesDescriptor moResourcesDescriptor;

	private PlugInConfigurations moPluginConfigurations;

	private boolean mbStopRequested;
	private boolean mbPauseRequested;
	private ThreadGroup moThreadGroup;
	private Thread moThread;
	private Throwable moFinalError;
	private List<IProcessorListener> maListeners;
	private IProcessorManager moParentProcessorManager;

	public ProcessorManager() {
		// Mandatory Configuration Directives
		initWorkingFolderPath();
		try {
			setMaxSimultaneousStep(0);
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("TODO");
		}
		try {
			setHardKillTimeout(0);
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("TODO");
		}

		// Optional Configuration Directives
		setBatchMode(false);
		setPreserveTemporaryFilesMode(false);
		setRunDryMode(false);
		setSequenceDescriptor(new SequenceDescriptor());
		setResourcesDescriptor(new ResourcesDescriptor());

		// Processing members
		setPluginConfigurations(new PlugInConfigurations());
		setStopRequested(false);
		setPauseRequested(false);
		setThreadGroup(null);
		setThread(null);
		setFinalError(null);
		setListeners(new ArrayList<IProcessorListener>());
		setTaskFactory(new TaskFactory(this));
		setParentProcessorManager(null);
	}

	private void initWorkingFolderPath() {
		msWorkingFolderPath = null;
	}

	private TaskFactory getTaskFactory() {
		return moTaskFactory;
	}

	private TaskFactory setTaskFactory(TaskFactory tf) {
		if (tf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid TaskFactory.");
		}
		TaskFactory previous = getTaskFactory();
		moTaskFactory = tf;
		return previous;
	}

	private IProcessorManager getParentProcessorManager() {
		return moParentProcessorManager;
	}

	private void setParentProcessorManager(IProcessorManager ppm) {
		// can be null, if it is the master ProcessorManager
		moParentProcessorManager = ppm;
	}

	@Override
	public String getWorkingFolderPath() {
		return msWorkingFolderPath;
	}

	@Override
	public String setWorkingFolderPath(String v)
			throws IllegalDirectoryException {
		FS.validateDirPath(v);
		String previous = msWorkingFolderPath;
		msWorkingFolderPath = v;
		return previous;
	}

	@Override
	public int getMaxSimultaneousStep() {
		return miMaxSimultaneousStep;
	}

	@Override
	public int setMaxSimultaneousStep(int v)
			throws ProcessorManagerConfigurationException {
		if (v < 0) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_HARD_KILL_TIMEOUT, v));
		}
		int previous = miMaxSimultaneousStep;
		miMaxSimultaneousStep = v;
		return previous;
	}

	@Override
	public int getHardKillTimeout() {
		return miHardKillTimeout;
	}

	@Override
	public int setHardKillTimeout(int v)
			throws ProcessorManagerConfigurationException {
		if (v < 0) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_MAX_PAR, v));
		}
		int previous = miHardKillTimeout;
		miHardKillTimeout = v;
		return previous;
	}

	@Override
	public boolean setBatchMode(boolean v) {
		boolean previous = mbBatchMode;
		mbBatchMode = v;
		return previous;
	}

	@Override
	public boolean enableBatchMode() {
		boolean previous = mbBatchMode;
		mbBatchMode = true;
		return previous;
	}

	@Override
	public boolean disableBatchMode() {
		boolean previous = mbBatchMode;
		mbBatchMode = false;
		return previous;
	}

	@Override
	public boolean isBatchModeEnable() {
		return mbBatchMode;
	}

	@Override
	public boolean setPreserveTemporaryFilesMode(boolean v) {
		boolean previous = mbPreserveTemporayFilesMode;
		mbPreserveTemporayFilesMode = v;
		return previous;
	}

	@Override
	public boolean enablePreserveTemporaryFilesMode() {
		boolean previous = mbPreserveTemporayFilesMode;
		mbPreserveTemporayFilesMode = true;
		return previous;
	}

	@Override
	public boolean disablePreserveTemporaryFilesMode() {
		boolean previous = mbPreserveTemporayFilesMode;
		mbPreserveTemporayFilesMode = false;
		return previous;
	}

	@Override
	public boolean isPreserveTemporaryFilesModeEnable() {
		return mbPreserveTemporayFilesMode;
	}

	@Override
	public boolean setRunDryMode(boolean v) {
		boolean previous = mbRunDryMode;
		mbRunDryMode = v;
		return previous;
	}

	@Override
	public boolean enableRunDryMode() {
		boolean previous = mbRunDryMode;
		mbRunDryMode = true;
		return previous;
	}

	@Override
	public boolean disableRunDryMode() {
		boolean previous = mbRunDryMode;
		mbRunDryMode = false;
		return previous;
	}

	@Override
	public boolean isRunDryModeEnable() {
		return mbRunDryMode;
	}

	@Override
	public SequenceDescriptor getSequenceDescriptor() {
		return moSequenceDescriptor;
	}

	public SequenceDescriptor setSequenceDescriptor(SequenceDescriptor sd) {
		if (sd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ISequenceDescriptor.");
		}
		SequenceDescriptor previous = moSequenceDescriptor;
		moSequenceDescriptor = sd;
		return previous;
	}

	@Override
	public ResourcesDescriptor getResourcesDescriptor() {
		return moResourcesDescriptor;
	}

	public ResourcesDescriptor setResourcesDescriptor(ResourcesDescriptor rds) {
		if (rds == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid IResourcesDescriptor.");
		}
		ResourcesDescriptor previous = getResourcesDescriptor();
		moResourcesDescriptor = rds;
		return previous;
	}

	@Override
	public IRegisteredTasks getRegisteredTasks() {
		return getTaskFactory().getRegisteredTasks();
	}

	public IRegisteredTasks setRegisteredTasks(IRegisteredTasks rts) {
		return getTaskFactory().setRegisteredTasks(rts);
	}

	@Override
	public PlugInConfigurations getPluginConfigurations() {
		return moPluginConfigurations;
	}

	public PlugInConfigurations setPluginConfigurations(PlugInConfigurations pcs) {
		if (pcs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Map<String, PropertiesSet>.");
		}
		PlugInConfigurations previous = getPluginConfigurations();
		moPluginConfigurations = pcs;
		return previous;
	}

	public IPlugInConfiguration getPluginConfiguration(
			Class<? extends IPlugInConfiguration> key)
			throws PlugInConfigurationException {
		IPlugInConfigurations pcs = getPluginConfigurations();
		IPlugInConfiguration pc = null;
		pc = pcs.get(key);
		if (pc == null) {
			throw new PlugInConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_NOT_REGISTERED, key));
		}
		return pc;
	}

	private boolean isSubPM() {
		return getParentProcessorManager() != null;
	}

	private boolean isSameSequenceDescriptorAsParent() {
		return isSubPM()
				&& getSequenceDescriptor().getFileFullPath().equals(
						getParentProcessorManager().getSequenceDescriptor()
								.getFileFullPath());
	}

	public IProcessorManager createSubProcessorManager(PropertiesSet ps) {
		ProcessorManager dest = new ProcessorManager();
		dest.setParentProcessorManager(this);
		dest.setRegisteredTasks(getRegisteredTasks());

		try {
			dest.setWorkingFolderPath(getWorkingFolderPath());
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexpected error while setting path "
					+ "of an sub-ProcessorManager. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. "
					+ "Or an external event made the path no more "
					+ "accessible (deleted, moved, read permission removed, "
					+ "...).", Ex);
		}

		try {
			dest.setHardKillTimeout(getHardKillTimeout());
			dest.setMaxSimultaneousStep(getMaxSimultaneousStep());
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "member of an sub-ProcessorManager. "
					+ "Since values are taken from another ProcessorManager, "
					+ "such error can not raise. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}

		dest.setListeners(getListeners());
		dest.setResourcesDescriptor(getResourcesDescriptor());
		dest.setPluginConfigurations(getPluginConfigurations());
		dest.setBatchMode(isBatchModeEnable());
		dest.setRunDryMode(isRunDryModeEnable());
		dest.setPreserveTemporaryFilesMode(isPreserveTemporaryFilesModeEnable());
		dest.getSequenceDescriptor().setProperties(ps.copy());

		return dest;
	}

	@Override
	public String toString() {
		String s;
		// Mandatory Configuration Directive
		s = "Working Folder Path" + "=" + getWorkingFolderPath()
				+ Util.NEW_LINE;
		s += "Max Simultaneous Order" + "=" + getMaxSimultaneousStep()
				+ Util.NEW_LINE;
		s += "Hard Kill Timeout" + "=" + getHardKillTimeout() + Util.NEW_LINE;

		// Optional Configuration Directive
		s += getResourcesDescriptor().toString() + Util.NEW_LINE;
		s += "Batch Mode" + "=" + isBatchModeEnable() + Util.NEW_LINE;
		s += "Preserve Temporary Resources Mode" + "="
				+ isPreserveTemporaryFilesModeEnable() + Util.NEW_LINE;
		s += "Run Dry Mode" + "=" + isRunDryModeEnable() + Util.NEW_LINE;
		s += "Sequence Descriptor File Path" + "="
				+ getSequenceDescriptor().getFileFullPath() + Util.NEW_LINE;
		s += "Order(s)" + "=";
		for (int i = 0; i < getSequenceDescriptor().countOrders(); i++)
			s += getSequenceDescriptor().getOrder(i) + " ";
		s += Util.NEW_LINE;
		return s;
	}

	/**
	 * <p>
	 * Throws a {@link ProcessorManagerConfigurationException} if this object is
	 * not correctly configured.
	 * </p>
	 * <p>
	 * Should be called after one or more member's update, in order to validate
	 * this object is correctly configured.
	 * </p>
	 * 
	 * @throws ProcessorManagerConfigurationException
	 *             if the Working Folder Path is not set.
	 * @throws ProcessorManagerConfigurationException
	 *             if the Sequence Descriptor is not set.
	 */
	private void validateConfigurationDirectives()
			throws ProcessorManagerConfigurationException {
		// If the Working Folder Path is not provided => raise an error
		if (getWorkingFolderPath() == null) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Working Folder Path"));
		}
		// If the Sequence Descriptor File Path is not provided => raise an
		// error
		if (getSequenceDescriptor().getFileFullPath() == null) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Sequence Descriptor Path"));
		}
	}

	private void setStopRequested(boolean bStopRequested) {
		mbStopRequested = bStopRequested;
	}

	private void setPauseRequested(boolean bPauseRequested) {
		mbPauseRequested = bPauseRequested;
	}

	@Override
	public synchronized boolean isStopRequested() {
		return mbStopRequested;
	}

	@Override
	public synchronized boolean isPauseRequested() {
		return mbPauseRequested;
	}

	@Override
	public synchronized boolean isRunning() {
		return getThread() != null;
	}

	private Thread getThread() {
		return moThread;
	}

	private Thread setThread(Thread v) {
		// Can be set to null, when cleaning
		Thread previous = getThread();
		moThread = v;
		return previous;
	}

	private ThreadGroup getThreadGroup() {
		return moThreadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be set to null, when cleaning
		ThreadGroup previous = getThreadGroup();
		moThreadGroup = tg;
		return previous;
	}

	private Throwable setFinalError(Throwable e) {
		// Can be set to null, when there is no error
		Throwable previous = getProcessingFinalError();
		moFinalError = e;
		return previous;
	}

	@Override
	public Throwable getProcessingFinalError() {
		return moFinalError;
	}

	@Override
	public void startProcessing() throws ProcessorManagerConfigurationException {
		startProcessing(null, 1);
	}

	@Override
	public synchronized void startProcessing(ThreadGroup ptg, int index)
			throws ProcessorManagerConfigurationException {
		// Perform cross controls on GlobalConfiguration instance
		validateConfigurationDirectives();
		// Fire the event
		fireRequestProcessorToStartEvent();
		// If the Thread is not null, that mean that the processing has already
		// been started
		if (getThread() != null) {
			return;
		}
		// Create its own ThreadGroup, so that all child Thread can be
		// interrupted easily
		String sTGName = "PM" + "-" + "main";
		if (ptg == null) {
			setThreadGroup(new ThreadGroup(sTGName));
		} else {
			sTGName = ptg.getName() + ">" + "PM" + "-" + index;
			setThreadGroup(new ThreadGroup(ptg, sTGName));
		}
		// Will be automatically destroyed
		getThreadGroup().setDaemon(true);
		// Create a dedicated Thread which will perform the processing
		setThread(new Thread(getThreadGroup(), this, sTGName));
		// Start the dedicated thread (call the method run - see Thread)
		getThread().start();
	}

	@Override
	public synchronized void stopProcessing() {
		fireRequestProcessorToStopEvent();
		mbStopRequested = true;
		// If the Thread is null, that mean that the processing has not been
		// started. In such condition, there is nothing to stop
		if (getThread() != null) {
			getThreadGroup().interrupt();
		}
	}

	@Override
	public synchronized void pauseProcessing() {
		fireRequestProcessorToPauseEvent();
		mbPauseRequested = true;
	}

	@Override
	public synchronized void resumeProcessing() {
		fireRequestProcessorToResumeEvent();
		if (!isPauseRequested())
			return;
		mbPauseRequested = false;
		notifyAll();
	}

	@Override
	public void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException {
		// If the Thread is null, that mean that the processing has not been
		// started. In such condition there is nothing to wait for
		if (getThread() == null)
			return;
		getThread().join(millis, nanos);
	}

	@Override
	public void waitTillProcessingIsDone(long millis)
			throws InterruptedException {
		waitTillProcessingIsDone(millis, 0);
	}

	@Override
	public void waitTillProcessingIsDone() throws InterruptedException {
		waitTillProcessingIsDone(0, 0);
	}

	@Override
	public boolean addListener(IProcessorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IProcessorListener.class.getCanonicalName() + ".");
		}
		return maListeners.add(l);
	}

	@Override
	public boolean removeListener(IProcessorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ IProcessorListener.class.getCanonicalName() + ".");
		}
		return maListeners.remove(l);
	}

	@Override
	public List<IProcessorListener> getListeners() {
		return maListeners;
	}

	@Override
	public List<IProcessorListener> setListeners(List<IProcessorListener> l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid List<"
					+ IProcessorListener.class.getCanonicalName() + ">.");
		}
		List<IProcessorListener> previous = maListeners;
		maListeners = l;
		return previous;
	}

	private void fireRequestProcessorToStartEvent() {
		RequestProcessingToStartEvent evt = null;
		evt = new RequestProcessingToStartEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStartRequested(evt);
		}
	}

	private void fireRequestProcessorToStopEvent() {
		RequestProcessingToStopEvent evt = null;
		evt = new RequestProcessingToStopEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStopRequested(evt);
		}
	}

	private void fireRequestProcessorToPauseEvent() {
		RequestProcessingToPauseEvent evt = null;
		evt = new RequestProcessingToPauseEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingPauseRequested(evt);
		}
	}

	private void fireRequestProcessorToResumeEvent() {
		RequestProcessingToResumeEvent evt = null;
		evt = new RequestProcessingToResumeEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingResumeRequested(evt);
		}
	}

	private void fireProcessorStartedEvent() {
		ProcessingStartedEvent evt = null;
		evt = new ProcessingStartedEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStarted(evt);
		}
	}

	private void fireProcessorFinishedEvent(State state, Throwable cause) {
		ProcessingFinishedEvent evt = null;
		evt = new ProcessingFinishedEvent(this, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.processingFinished(evt);
		}
	}

	private void fireTaskCreatedEvent(String taskName, State state,
			Throwable cause) {
		TaskCreatedEvent evt = null;
		evt = new TaskCreatedEvent(taskName, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.taskCreated(evt);
		}
	}

	private void fireTaskStartedEvent(ITask t) {
		TaskStartedEvent evt = null;
		evt = new TaskStartedEvent(t);
		for (IProcessorListener l : getListeners()) {
			l.taskStarted(evt);
		}
	}

	private void fireTaskFinishedEvent(ITask t, State state, Throwable cause) {
		TaskFinishedEvent evt = null;
		evt = new TaskFinishedEvent(t, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.taskFinished(evt);
		}
	}

	public synchronized void handleProcessorStateUpdates()
			throws InterruptedException {
		while (true) {
			if (isStopRequested()) {
				throw new InterruptedException("Task processing interrupted");
			} else if (isPauseRequested())
				try {
					wait(1000);
				} catch (InterruptedException Ex) {
					throw new InterruptedException("Pause interrupted");
				}
			else {
				break;
			}
		}
	}

	@Override
	public void run() {
		try {
			fireProcessorStartedEvent();
			processSequenceDescriptor();
			fireProcessorFinishedEvent(State.SUCCESS, null);
		} catch (InterruptedException Ex) {
			InterruptedException e = new InterruptedException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.INTERRUPTED,
					getSequenceDescriptor().getFileFullPath()));
			fireProcessorFinishedEvent(State.INTERRUPTED, e);
			setFinalError(e);
		} catch (TaskException Ex) {
			ProcessorException e = new ProcessorException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.FAILED,
					getSequenceDescriptor().getFileFullPath()), Ex);
			fireProcessorFinishedEvent(State.FAILED, e);
			if (isSameSequenceDescriptorAsParent()) {
				setFinalError(Ex);
			} else {
				setFinalError(e);
			}
		} catch (Throwable Ex) {
			ProcessorException e = new ProcessorException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.CRITICAL,
					getSequenceDescriptor().getFileFullPath()), Ex);
			fireProcessorFinishedEvent(State.CRITICAL, e);
			setFinalError(e);
		} finally {
			if (!isSubPM()) {
				getResourcesDescriptor().store();
				deleteTemporaryResources();
			}
			// The thread is set to null, so it is possible to start the
			// processing again (it will certainly be useful to somebody)
			synchronized (this) {
				setThread(null);
				setThreadGroup(null);
			}
		}
	}

	private void processSequenceDescriptor() throws TaskException,
			InterruptedException {
		ITask sequence = newTask(getSequenceDescriptor().getRoot(),
				getSequenceDescriptor().getProperties());
		processTask(sequence);
	}

	private void deleteTemporaryResources() {
		try {
			if (isPreserveTemporaryFilesModeEnable()) {
				log.debug("Temporary resources not cleaned "
						+ "('Preserve Temporary Files Mode' is enabled).");
				return;
			}
			if (getWorkingFolderPath() != null) {
				FS.deleteDirectoryAndEmptyParentDirectory(getWorkingFolderPath());
			}
			log.debug("Temporary resources cleaned.");
		} catch (Throwable Ex) {
			log.warn(Util.getUserFriendlyStackTrace(new Exception(
					"Failed to delete temporary resources.", Ex)));
		}
	}

	public synchronized ITask newTask(Node n, PropertiesSet ps)
			throws TaskException {
		try {
			ITask t = getTaskFactory().newTask(n, ps);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.SUCCESS,
					null);
			return t;
		} catch (TaskFactoryException Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_INIT_FINAL_STATE,
					new Object[] { n.getNodeName().toLowerCase(), State.FAILED,
							Doc.getNodeLocation(n) }), Ex);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.FAILED, e);
			throw e;
		} catch (Throwable Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_INIT_FINAL_STATE,
					new Object[] { n.getNodeName().toLowerCase(),
							State.CRITICAL, Doc.getNodeLocation(n) }), Ex);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.CRITICAL,
					e);
			throw e;
		}
	}

	public void processTask(ITask task) throws TaskException,
			InterruptedException {
		try {
			fireTaskStartedEvent(task);
			task.doProcessing();
			fireTaskFinishedEvent(task, State.SUCCESS, null);
		} catch (InterruptedException Ex) {
			InterruptedException e = new InterruptedException(Messages.bind(
					Messages.TaskEx_PROCESS_FINAL_STATE,
					new Object[] {
							task.getClass().getSimpleName().toLowerCase(),
							State.INTERRUPTED,
							Doc.getNodeLocation(task.getContext().getNode()) }));
			fireTaskFinishedEvent(task, State.INTERRUPTED, e);
			throw e;
		} catch (TaskException Ex) {
			TaskException e = new TaskException(
					Messages.bind(
							Messages.TaskEx_PROCESS_FINAL_STATE,
							new Object[] {
									task.getClass().getSimpleName()
											.toLowerCase(),
									State.FAILED,
									Doc.getNodeLocation(task.getContext()
											.getNode()) }), Ex);
			fireTaskFinishedEvent(task, State.FAILED, e);
			if (Ex instanceof SequenceException) {
				throw Ex;
			} else {
				throw e;
			}
		} catch (Throwable Ex) {
			TaskException e = new TaskException(
					Messages.bind(
							Messages.TaskEx_PROCESS_FINAL_STATE,
							new Object[] {
									task.getClass().getSimpleName()
											.toLowerCase(),
									State.CRITICAL,
									Doc.getNodeLocation(task.getContext()
											.getNode()) }), Ex);
			fireTaskFinishedEvent(task, State.CRITICAL, e);
			throw e;
		}
	}
}