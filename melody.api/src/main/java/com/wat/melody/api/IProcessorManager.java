package com.wat.melody.api;

import java.util.List;
import java.util.Map;

import com.wat.melody.api.exception.ProcessorManagerConfigurationException;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;

/**
 * <p>
 * This interface is specially designed to start, pause, resume and stop the
 * processing, as describe in a Sequence Descriptor.
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
public interface IProcessorManager {

	public String getWorkingFolderPath();

	public String setWorkingFolderPath(String v)
			throws IllegalDirectoryException;

	public int getMaxSimultaneousStep();

	public int setMaxSimultaneousStep(int v)
			throws ProcessorManagerConfigurationException;

	public int getHardKillTimeout();

	public int setHardKillTimeout(int v)
			throws ProcessorManagerConfigurationException;

	public boolean setBatchMode(boolean v);

	public boolean enableBatchMode();

	public boolean disableBatchMode();

	public boolean isBatchModeEnable();

	public boolean setPreserveTemporaryFilesMode(boolean v);

	public boolean enablePreserveTemporaryFilesMode();

	public boolean disablePreserveTemporaryFilesMode();

	public boolean isPreserveTemporaryFilesModeEnable();

	public boolean setRunDryMode(boolean v);

	public boolean enableRunDryMode();

	public boolean disableRunDryMode();

	public boolean isRunDryModeEnable();

	public IProcessorManager createSubProcessorManager(PropertiesSet ps);

	public ISequenceDescriptor getSequenceDescriptor();

	public IResourcesDescriptor getResourcesDescriptor();

	public Map<String, IPluginConfiguration> getPluginConfigurations();

	public IRegisteredTasks getRegisteredTasks();

	/**
	 * <p>
	 * Validate this object is correctly configured and start the processing in
	 * a new dedicated thread.
	 * </p>
	 * 
	 * <p>
	 * <i> The new Thread will be created in the current ThreadGroup. <BR/>
	 * * Don't do anything if the processing has already been started. <BR/>
	 * * Call {@link #waitTillProcessingIsDone()} to wait for the end of the
	 * processing. <BR/>
	 * * The processing can also be paused ({@link #pauseProcessing()}, resumed
	 * ({@link #resumeProcessing()}) or stopped ({@link #stopProcessing()}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws ProcessorManagerConfigurationException
	 *             if this object is not correctly configured.
	 * 
	 */
	public void startProcessing() throws ProcessorManagerConfigurationException;

	/**
	 * <p>
	 * Validate this object is correctly configured and start the processing in
	 * a new dedicated thread.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the given ThreadGroup is <code>null</code>, the new Thread will
	 * be created in the current ThreadGroup. <BR/>
	 * * If the given ThreadGroup is not <code>null</code>, the new Thread will
	 * be created in the given ThreadGroup. <BR/>
	 * * Don't do anything if the processing has already been started. <BR/>
	 * * Call {@link #waitTillProcessingIsDone()} to wait for the end of the
	 * processing. <BR/>
	 * * The processing can also be paused ({@link #pauseProcessing()}, resumed
	 * ({@link #resumeProcessing()}) or stopped ({@link #stopProcessing()}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws ProcessorManagerConfigurationException
	 *             if this object is not correctly configured.
	 * 
	 */
	public void startProcessing(ThreadGroup ptg, int index)
			throws ProcessorManagerConfigurationException;

	public boolean isRunning();

	/**
	 * <p>
	 * Request the processing to stop.
	 * </p>
	 * 
	 * <p>
	 * <i> * Once stopped, the processing can no more be paused or resumed. <BR/>
	 * * Call {@link #waitTillProcessingIsDone()} to wait for the end of the
	 * processing. <BR/>
	 * * When finished, the engine will send an EngineFinishedEvent to call
	 * listener. Look at the value of the final state in the event to know if
	 * the engine processing finished successfully or not. <BR/>
	 * * If the processing didn't finished successfully, call
	 * {@link #getProcessingFinalError()} to get the Exception which causes the
	 * processing to finished. <BR/>
	 * </i>
	 * </p>
	 * 
	 */
	public void stopProcessing();

	public boolean isStopRequested();

	/**
	 * <p>
	 * Pause the processing.
	 * </p>
	 * 
	 * <p>
	 * <i> The processing can be resumed (see {@link #resumeProcessing()}) or
	 * stopped (see {@link #stopProcessing()}). </i>
	 * </p>
	 * 
	 */
	public void pauseProcessing();

	public boolean isPauseRequested();

	/**
	 * <p>
	 * Resume the processing.
	 * </p>
	 * 
	 * <p>
	 * <i> The processing must have been paused previously (see
	 * {@link #pauseProcessing()}). </i>
	 * </p>
	 * 
	 */
	public void resumeProcessing();

	/**
	 * <p>
	 * Wait for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i> * The processing can finished naturally (once everything have been
	 * done) or can be stopped (see{@link #stopProcessing()}). <BR/>
	 * * Once the processing is done, call {@link #getProcessingFinalError()} to
	 * get the informations about the processing state. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the engine's thread was interrupted.
	 * 
	 */
	public void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException;

	/**
	 * <p>
	 * Wait for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i> * The processing can finished naturally (once everything have been
	 * done) or can be stopped (see{@link #stopProcessing()}). <BR/>
	 * * Once the processing is done, call {@link #getProcessingFinalError()} to
	 * get the informations about the processing state. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the engine's thread was interrupted.
	 * 
	 */
	public void waitTillProcessingIsDone(long millis)
			throws InterruptedException;

	/**
	 * <p>
	 * Wait for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i> * The processing can finished naturally (once everything have been
	 * done) or can be stopped (see{@link #stopProcessing()}). <BR/>
	 * * Once the processing is done, call {@link #getProcessingFinalError()} to
	 * get the informations about the processing state. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the engine's thread was interrupted.
	 * 
	 */
	public void waitTillProcessingIsDone() throws InterruptedException;

	/**
	 * <p>
	 * Retrieve informations about the processing state.
	 * </p>
	 * 
	 * @return a Throwable object, which contains the error which occurred
	 *         during the processing, or null if the processing ended without
	 *         any errors.
	 * 
	 */
	public Throwable getProcessingFinalError();

	public boolean addListener(IProcessorListener l);

	public boolean removeListener(IProcessorListener l);

	public List<IProcessorListener> getListeners();

	public List<IProcessorListener> setListeners(List<IProcessorListener> l);

}