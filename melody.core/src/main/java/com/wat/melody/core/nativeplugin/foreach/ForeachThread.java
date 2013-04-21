package com.wat.melody.core.nativeplugin.foreach;

import java.lang.Thread.State;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.MelodyThread;
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

	private PropertiesSet _propertiesSet;
	private Foreach _foreach;
	private MelodyThread _thread;
	private Throwable _finalError;

	/**
	 * <p>
	 * Create a new {@link ForeachThread} object, which is especially designed
	 * to process all inner-task defined in the given {@link Foreach} Task
	 * against a specific target.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>The processing can be executed synchronously (see {@link #run()}) -
	 * in the current thread - or asynchronously (see {@link #startProcessing()}
	 * ) - in a dedicated thread - ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param p
	 *            is the {@link Foreach} Task which contains all inner-task to
	 *            proceed.
	 * @param ps
	 *            is a dedicated {@link PropertiesSet}, which will be used
	 *            during {@link Foreach} inner-task variable's expansion.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Foreach} Task is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link PropertiesSet} Task is <tt>null</tt>.
	 * 
	 * @see {@link #run()}
	 * @see {@link #startProcessing()}
	 */
	public ForeachThread(Foreach p, PropertiesSet ps) {
		setPropertiesSet(ps);
		setForeach(p);
		setFinalError(null);
		setThread(Melody.createNewMelodyThread(p.getThreadGroup(), this, p
				.getThreadGroup().getName()
				+ "-"
				+ (p.getThreadsList().size() + 1)));
		getThread().pushContext(Melody.getContext());
	}

	/**
	 * <p>
	 * Get the state of the processing.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>If the processing failed, call {@link #getFinalError()} to retrieve
	 * the exception that causes the failure :</li>
	 * </ul>
	 * </p>
	 * 
	 * @return <ul>
	 *         <li>{@link Foreach.NEW} if the processing has not been started
	 *         yet (see {@link #startProcessing()}) ;</li>
	 *         <li>{@link Foreach#RUNNING} if the processing has been started
	 *         but is not finished ;</li>
	 *         <li>{@link Foreach#SUCCEED} if the processing finished
	 *         successfully ;</li>
	 *         <li>{@link Foreach#INTERRUPTED} if the processing has been
	 *         interrupted ;</li>
	 *         <li>{@link Foreach#FAILED} if an error occurred during the
	 *         processing ;</li>
	 *         <li>{@link Foreach#CRITICAL} if an unmanaged error occurred
	 *         during the processing ;</li>
	 *         </ul>
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
	 * <ul>
	 * <li>The dedicated thread is created in the {@link ForeachEach}'s thread
	 * group, meaning that if the {@link ForeachEach}'s thread group is
	 * interrupted, the dedicated thread will be interrupted too ;</li>
	 * <li>The processing can only be started one time. Later call to this
	 * method will raise an <tt>IllegalThreadStateException</tt> ;</li>
	 * <li>After it has been started, call {@link #waitTillProcessingIsDone()}
	 * to wait for the processing to end ;</li>
	 * </ul>
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
	 * <ul>
	 * <li>While waiting, this method doesn't consume CPU ;</li>
	 * <li>After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not ;</li>
	 * <li>If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately.</li>
	 * </ul>
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
	 * Waits at most <tt>millis</tt> milliseconds for the processing to end. If
	 * <tt>millis</tt> is equal to 0, this method will wait forever for the
	 * processing to end.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>While waiting, this method doesn't consume CPU ;</li>
	 * <li>After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not ;</li>
	 * <li>If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param millis
	 *            is the time to wait in milliseconds.
	 * 
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * @throws IllegalArgumentException
	 *             if <tt>millis</tt> is negative.
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
	 * Waits at most <tt>millis</tt> milliseconds + <tt>nanos</tt> nanoseconds
	 * for the processing to end. If <tt>millis</tt> and <tt>nanos</tt> are
	 * equal to 0, this method will wait forever for the processing to end.
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>While waiting, this method doesn't consume CPU ;</li>
	 * <li>After a call to this method, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not ;</li>
	 * <li>If the processing as not yet been started (see
	 * {@link #startProcessing()}), this method will return immediately.</li>
	 * </ul>
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
	 *             if <tt>millis</tt> is negative and/or if <tt>nanos</tt> is
	 *             out of the range 0-999999.
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
	 * <ul>
	 * <li>To start the processing in a dedicated thread, call
	 * {@link #startProcessing()} ;</li>
	 * <li>As soon as it is finished, call {@link #getFinalState()} to know if
	 * the processing is finished successfully or not ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @see {@link #getFinalState()}
	 * @see {@link #startProcessing()}
	 */
	public void run() {
		try {
			for (Node n : getForeach().getInnerTasks()) {
				Melody.getContext().processTask(n, getPropertiesSet());
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		}
	}

	private PropertiesSet getPropertiesSet() {
		return _propertiesSet;
	}

	/**
	 * <p>
	 * Set the {@link PropertiesSet} of this object, which contains the
	 * appropriate 'item' definition.
	 * </p>
	 * 
	 * @param a
	 *            is the {@link PropertiesSet} to set.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link PropertiesSet} is <tt>null</tt>.
	 * 
	 */
	private void setPropertiesSet(PropertiesSet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ PropertiesSet.class.getCanonicalName() + ".");
		}
		_propertiesSet = ps;
	}

	private Foreach getForeach() {
		return _foreach;
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
	 *             if the given {@link Foreach} is <tt>null</tt>.
	 * 
	 */
	private Foreach setForeach(Foreach p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Foreach.class.getCanonicalName()
					+ ".");
		}
		return _foreach = p;
	}

	private MelodyThread getThread() {
		return _thread;
	}

	private MelodyThread setThread(MelodyThread t) {
		return _thread = t;
	}

	/**
	 * <p>
	 * Get the exception that causes the processing to fail.
	 * </p>
	 * 
	 * @return <ul>
	 *         <li><tt>null</tt> if the processing has not been started yet (see
	 *         {@link #startProcessing()}) ;</li>
	 *         <li><tt>null</tt> if the processing has been started but is not
	 *         finished and no error has occurred yet ;</li>
	 *         <li><tt>null</tt> if the processing finished successfully ;</li>
	 *         <li>an object of the class {@link InterruptedException} if the
	 *         processing has been interrupted ;</li>
	 *         <li>an object of the class {@link TaskException} if an error
	 *         occurred during the processing ;</li>
	 *         <li>an object of the class {@link Throwable} if an unmanaged
	 *         error occurred during the processing ;</li>
	 *         </ul>
	 * 
	 * @see {@link #startProcessing()}
	 */
	public Throwable getFinalError() {
		return _finalError;
	}

	private Throwable setFinalError(Throwable e) {
		return _finalError = e;
	}

}