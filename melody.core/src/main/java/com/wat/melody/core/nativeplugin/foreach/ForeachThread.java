package com.wat.melody.core.nativeplugin.foreach;

import java.lang.Thread.State;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.properties.PropertiesSet;

/**
 * <p>
 * Process all {@link ITask} defined in the given {@link Foreach}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ForeachThread implements Runnable {

	private PropertiesSet moPropertiesSet;
	private Foreach moForeach;
	private Thread moThread;
	private Throwable moFinalError;

	/**
	 * <p>
	 * Create a new <code>ForeachThread</code> object, which is especially
	 * designed to process all inner-<code>Task</code> defined in the given
	 * <code>Foreach</code> Task.
	 * </p>
	 * 
	 * <p>
	 * <i>* The processing can be executed synchronously (see {@link #run()}) or
	 * asynchronously (see {@link #startProcessing()}). </i>
	 * </p>
	 * 
	 * @param p
	 *            is the <code>Foreach</code> Task which contains all inner-
	 *            <code>Task</code> to proceed.
	 * @param index
	 *            is the index of the inner thread created by this object.
	 * @param ps
	 *            is a dedicated PropertiesSet, which will be used during
	 *            variable's expansion.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <code>Task</code> list is null.
	 * @throws IllegalArgumentException
	 *             if one of the <code>Task</code> in the <code>Task</code> list
	 *             is null.
	 * @throws IllegalArgumentException
	 *             if the given <code>Foreach</code> is null.
	 * 
	 * @see {@link #run()}
	 * @see {@link #startProcessing()}
	 */
	public ForeachThread(Foreach p, int index, PropertiesSet ps) {
		setPropertiesSet(ps);
		setForeach(p);
		setThread(new Thread(p.getThreadGroup(), this, p.getThreadGroup()
				.getName() + "-" + index));
		initFinalError();
	}

	private void initFinalError() {
		moFinalError = null;
	}

	/**
	 * <p>
	 * Get the state of the processing.
	 * </p>
	 * 
	 * <p>
	 * <i>* If the processing failed, call {@link #getFinalError()} to retrieve
	 * the exception that causes the failure. </i>
	 * </p>
	 * 
	 * @return {@link Foreach.NEW} if the processing has not been started yet
	 *         (see {@link #startProcessing()}). <BR/>
	 *         {@link Foreach#RUNNING} if the processing has been started but is
	 *         not finished. <BR/>
	 *         {@link Foreach#SUCCEED} if the processing finished successfully. <BR/>
	 *         {@link Foreach#INTERRUPTED} if the processing has been
	 *         interrupted. <BR/>
	 *         {@link Foreach#FAILED} if an error occurred during the
	 *         processing. <BR/>
	 *         {@link Foreach#CRITICAL} if an unmanaged error occurred during
	 *         the processing.
	 * 
	 * @see {@link #getFinalError()}
	 * @see {@link #startProcessing()}
	 */
	public short getFinalState() {
		if (getThread().getState() == State.NEW) {
			return Foreach.NEW;
		} else if (getThread().getState() != State.TERMINATED) {
			return Foreach.RUNNING;
		} else if (getFinalError() == null) {
			return Foreach.SUCCEED;
		} else if (getFinalError() instanceof MelodyException) {
			return Foreach.FAILED;
		} else if (getFinalError() instanceof InterruptedException) {
			return Foreach.INTERRUPTED;
		} else {
			return Foreach.CRITICAL;
		}
	}

	/**
	 * <p>
	 * Start the processing in a dedicated thread.
	 * </p>
	 * 
	 * <p>
	 * <i>* The dedicated thread is created in the <code>ForeachEach</code>'s
	 * thread group (see {@link #ForeachThread(List<Node>, Processor)}), meaning
	 * that if the <code>ForeachEach</code>'s thread group is interrupted, the
	 * dedicated thread will be interrupted too. <BR/>
	 * * The processing can only be started one time. Future call to this method
	 * will raise an <code>IllegalThreadStateException</code>. <BR/>
	 * * After a call to this method, call {@link #waitTillProcessingIsDone()}
	 * to wait for the processing to end. </i>
	 * </p>
	 * 
	 * @throws IllegalThreadStateException
	 *             if the processing has already been started.
	 * 
	 * @see {@link #waitTillProcessingIsDone()}
	 * @see {@link #waitTillProcessingIsDone(long)}
	 * @see {@link #waitTillProcessingIsDone(long, int)}
	 */
	public void startProcessing() {
		getThread().start();
	}

	/**
	 * <p>
	 * Waits for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i>* While waiting, this method doesn't consume CPU. <BR/>
	 * * After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not. <BR/>
	 * * If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately. </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * 
	 * @see {@link #getFinalState()}
	 * @see {@link #startProcessing()}
	 * @see {@link #waitTillProcessingIsDone(long)}
	 * @see {@link #waitTillProcessingIsDone(long, int)}
	 */
	public void waitTillProcessingIsDone() throws InterruptedException {
		waitTillProcessingIsDone(0, 0);
	}

	/**
	 * <p>
	 * Waits at most <code>millis</code> milliseconds for the processing to end.
	 * <BR/>
	 * If <code>millis</code> is equal to 0, this method will wait forever for
	 * the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i>* While waiting, this method doesn't consume CPU. <BR/>
	 * * After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished or not. <BR/>
	 * * If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately. </i>
	 * </p>
	 * 
	 * @param millis
	 *            is the time to wait in milliseconds.
	 * 
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * @throws IllegalArgumentException
	 *             if <code>millis</code> is negative.
	 * 
	 * @see {@link #getFinalState()}
	 * @see {@link #startProcessing()}
	 * @see {@link #waitTillProcessingIsDone()}
	 * @see {@link #waitTillProcessingIsDone(long, int)}
	 */
	public void waitTillProcessingIsDone(long millis)
			throws InterruptedException {
		waitTillProcessingIsDone(millis, 0);
	}

	/**
	 * <p>
	 * Waits at most <code>millis</code> milliseconds + <code>nanos</code>
	 * nanoseconds for the processing to end. <BR/>
	 * If <code>millis</code> and <code>nanos</code> are equal to 0, this method
	 * will wait forever for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <i>* While waiting, this method doesn't consume CPU. <BR/>
	 * * After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished or not. <BR/>
	 * * If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately. </i>
	 * </p>
	 * 
	 * @param millis
	 *            is the time to wait in milliseconds.
	 * @param nanos
	 *            is an 0-999999 additional nanoseconds to wait.
	 * 
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * @throws IllegalArgumentException
	 *             if <code>millis</code> is negative and/or if
	 *             <code>nanos</code> is out of the range 0-999999.
	 * 
	 * @see {@link #getFinalState()}
	 * @see {@link #startProcessing()}
	 * @see {@link #waitTillProcessingIsDone()}
	 * @see {@link #waitTillProcessingIsDone(long)}
	 */
	public void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException {
		getThread().join(millis, nanos);
	}

	/**
	 * <p>
	 * Start the processing (in the current thread).
	 * </p>
	 * 
	 * <p>
	 * <i>* To start the processing in a dedicated thread, call
	 * {@link #startProcessing()}. <BR/>
	 * * When the processing is done, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not. </i>
	 * </p>
	 * 
	 * @see {@link #getFinalState()}
	 * @see {@link #startProcessing()}
	 */
	public void run() {
		try {
			for (Node n : getForeach().getNodes()) {
				getForeach().getContext().processTask(n, getPropertiesSet());
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		}
	}

	private PropertiesSet getPropertiesSet() {
		return moPropertiesSet;
	}

	/**
	 * <p>
	 * Set the PropertiesSet of this object, which contains the appropriate
	 * 'item' definition.
	 * </p>
	 * 
	 * @param a
	 *            is the {@link PropertiesSet} to set.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link PropertiesSet} is <code>null</code>.
	 * 
	 */
	private void setPropertiesSet(PropertiesSet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		moPropertiesSet = ps;
	}

	private Foreach getForeach() {
		return moForeach;
	}

	/**
	 * <p>
	 * Set the parent {@link Foreach} of this object.
	 * </p>
	 * 
	 * @param a
	 *            is the parent {@link Foreach} to set.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Foreach} is <code>null</code>.
	 * 
	 */
	private Foreach setForeach(Foreach p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Foreach.");
		}
		return moForeach = p;
	}

	private Thread getThread() {
		return moThread;
	}

	private Thread setThread(Thread t) {
		return moThread = t;
	}

	/**
	 * <p>
	 * Get the exception that causes the processing to fail.
	 * </p>
	 * 
	 * @return <code>null</code> if the processing has not been started yet (see
	 *         {@link #startProcessing()}). <BR/>
	 *         <code>null</code> if the processing has been started but is not
	 *         finished and no error has occurred yet. <BR/>
	 *         <code>null</code> if the processing finished successfully. <BR/>
	 *         an object of the class {@link InterruptedException} if the
	 *         processing has been interrupted. <BR/>
	 *         an object of the class {@link TaskException} if an error occurred
	 *         during the processing. <BR/>
	 *         an object of the class {@link Throwable} if an unmanaged error
	 *         occurred during the processing.
	 * 
	 * @see {@link #startProcessing()}
	 */
	public Throwable getFinalError() {
		return moFinalError;
	}

	private Throwable setFinalError(Throwable e) {
		return moFinalError = e;
	}

}