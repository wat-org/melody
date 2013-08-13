package com.wat.melody.core.nativeplugin.call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.ProcessorManagerConfigurationException;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.core.nativeplugin.call.exception.CallException;
import com.wat.melody.core.nativeplugin.foreach.ForeachThread;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Call extends Ref implements ITask {

	/**
	 * The 'call' XML Element
	 */
	public static final String CALL = "call";

	/**
	 * The 'ref' XML Nested Element
	 */
	public static final String REF = "ref";

	private static final short SUCCEED = 0;
	private static final short FAILED = 1;
	private static final short INTERRUPTED = 2;
	private static final short CRITICAL = 4;

	private List<Ref> _callRefs;

	private short _state;
	private ThreadGroup _threadGroup;
	private ConsolidatedException _exceptionsSet;

	/**
	 * <p>
	 * Create a new {@link Call} object, which is especially designed to launch
	 * the processing of Orders defined in another Sequence Descriptor file.
	 * </p>
	 */
	public Call() {
		super();
		setRelatedCall(this);
		setCallRefs(new ArrayList<Ref>());
		markState(SUCCEED);
		setThreadGroup(null);
		setExceptionsSet(new ConsolidatedException());
	}

	/**
	 * <p>
	 * Create a new {@link Ref} object, which stores data necessary to launch
	 * the processing of Orders defined in another Sequence Descriptor file.
	 * </p>
	 * 
	 * @return a new {@link Ref} object.
	 */
	@NestedElement(name = REF, type = NestedElement.Type.CREATE)
	public Ref createRef() {
		Ref cr = new Ref(this);
		getCallRefs().add(cr);
		return cr;
	}

	/**
	 * <p>
	 * Validates the content of this object and the content of inner {@link Ref}
	 * objects.
	 * </p>
	 * <p>
	 * <i> * If no alternative Sequence Descriptor file have been explicitly
	 * defined (in the '{@value Ref#SD_ATTR} ' XML attribute), the current
	 * Sequence Descriptor file will be used. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws CallException
	 *             if the no Order are defined, neither in this {@link Call}
	 *             object, nor in the inner {@link Ref} objects.
	 * @throws CallException
	 *             if one order is not valid (e.g. doesn't respect the order
	 *             syntax, doesn't match any order defined in the Sequence
	 *             Descriptor file, ...).
	 * @throws IOException
	 *             {@inheritDoc}
	 */
	@Override
	public void validate() throws CallException {
		for (Ref cr : getCallRefs()) {
			getIProcessorManagers().addAll(cr.getIProcessorManagers());
		}
		getCallRefs().clear();
		if (getIProcessorManagers().size() == 0) {
			throw new CallException(Msg.bind(Messages.CallEx_MISSING_REF, CALL,
					ORDERS_ATTR, REF));
		}
		// Validate all sub-ProcessorManager
		for (IProcessorManager pm : getIProcessorManagers()) {
			if (pm.getSequenceDescriptor().countOrders() == 0) {
				throw new CallException(Msg.bind(
						Messages.CallEx_MISSING_ORDERS, ORDERS_ATTR, REF));
			}
			if (pm.getSequenceDescriptor().getSourceFile() != null) {
				continue;
			}
			try {
				pm.getSequenceDescriptor().load(
						Melody.getContext().getProcessorManager()
								.getSequenceDescriptor());
			} catch (IllegalOrderException Ex) {
				throw new CallException(Ex);
			}
		}
	}

	private short markState(short s) {
		return _state |= s;
	}

	private boolean isFailed() {
		return FAILED == (_state & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (_state & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (_state & CRITICAL);
	}

	/**
	 * <p>
	 * Process the <code>Call</code> Task.
	 * </p>
	 * <p>
	 * <i> * All orders (defined neither in the 'orders' XML attribute of the
	 * 'call' XML element, or in the 'orders' XML attribute of an 'ref' nested
	 * element) will be proceed in its own sub-ProcessorManager. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws CallException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 */
	@Override
	public void doProcessing() throws CallException, InterruptedException {
		try {
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">" + CALL));
			getThreadGroup().setDaemon(true);
			try {
				startProcessing();
			} catch (InterruptedException Ex) {
				getExceptionsSet().addCause(Ex);
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptionsSet().addCause(Ex);
				markState(FAILED);
			} finally {
				// Even if an error occurred while creating and starting thread,
				// some thread may have been launched without any problem
				// We must wait for these threads to finished
				waitForProcessingToBeDone();
				quit();
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	/**
	 * <p>
	 * Start all sub-ProcessorManager is this object <code>ThreadGroup</code>.
	 * </p>
	 * <p>
	 * <i> * Each order (defined neither in the 'orders' XML attribute of the
	 * 'call' XML element, or in the 'orders' XML attribute of an 'ref' nested
	 * element) is proceed in its own sub-ProcessorManager. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws ProcessorManagerConfigurationException
	 *             if a configuration issue made a sub-ProcessorManager to fail
	 *             to start.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 */
	private void startProcessing()
			throws ProcessorManagerConfigurationException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		int index = 1;
		for (IProcessorManager pm : getIProcessorManagers()) {
			pm.startProcessing(getThreadGroup(), index++);
		}
	}

	/**
	 * <p>
	 * Wait for all sub-ProcessorManager to end.
	 * </p>
	 * <p>
	 * <i> * Will propagate the pause to all sub-ProcessorManager. <BR/>
	 * * Will wait for all sub-ProcessorManager to stop if the processing is
	 * stopped. <BR/>
	 * </i>
	 * </p>
	 */
	private void waitForProcessingToBeDone() {
		int nbTry = 2;
		while (nbTry > 0) {
			boolean running = false;
			for (IProcessorManager pm : getIProcessorManagers()) {
				if (pm.isRunning()) {
					running = true;
					break;
				}
			}
			if (!running) {
				return;
			}
			if (Melody.getContext().getProcessorManager().isPauseRequested()) {
				// if the processor is paused => propagate the pause to all
				// sub-ProcessorManager
				for (IProcessorManager pm : getIProcessorManagers()) {
					pm.pauseProcessing();
				}
				// while the processor is paused => sleep
				while (Melody.getContext().getProcessorManager()
						.isPauseRequested()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException Ex) {
						markState(INTERRUPTED);
						nbTry--;
						break;
					}
				}
				// if the pause ended and if it was not because of a stop =>
				// resume all sub-ProcessorManager
				if (!Melody.getContext().getProcessorManager()
						.isStopRequested()) {
					for (IProcessorManager pm : getIProcessorManagers()) {
						pm.resumeProcessing();
					}
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
				nbTry--;
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + CALL + " inner Task to finish.");
	}

	/**
	 * <p>
	 * Inspect all sub-ProcessorManager final state and raise the appropriate
	 * exception.
	 * </p>
	 * 
	 * @throws CallException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 */
	private void quit() throws CallException, InterruptedException {
		for (IProcessorManager pm : getIProcessorManagers()) {
			Throwable ex = pm.getProcessingFinalError();
			if (ex == null) {
				continue;
			} else if (ex instanceof InterruptedException) {
				getExceptionsSet().addCause(ex);
				markState(INTERRUPTED);
			} else if (ex instanceof MelodyException) {
				getExceptionsSet().addCause(ex);
				markState(FAILED);
			} else {
				getExceptionsSet().addCause(ex);
				markState(CRITICAL);
			}
		}

		if (isCritical()) {
			throw new CallException(getExceptionsSet());
		} else if (isFailed()) {
			throw new CallException(getExceptionsSet());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(getExceptionsSet());
		}
	}

	private List<Ref> getCallRefs() {
		return _callRefs;
	}

	private List<Ref> setCallRefs(List<Ref> cr) {
		if (cr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Ref>.");
		}
		List<Ref> previous = getCallRefs();
		_callRefs = cr;
		return previous;
	}

	/**
	 * @return the {@link ThreadGroup} which holds all {@link ForeachThread}
	 *         managed by this object.
	 */
	protected ThreadGroup getThreadGroup() {
		return _threadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		_threadGroup = tg;
		return previous;
	}

	/**
	 * @return the exceptions that append during the processing of this object.
	 */
	private ConsolidatedException getExceptionsSet() {
		return _exceptionsSet;
	}

	private ConsolidatedException setExceptionsSet(ConsolidatedException cex) {
		if (cex == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConsolidatedException.class.getCanonicalName() + ".");
		}
		ConsolidatedException previous = getExceptionsSet();
		_exceptionsSet = cex;
		return previous;
	}

}