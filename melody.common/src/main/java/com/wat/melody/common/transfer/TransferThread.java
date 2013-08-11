package com.wat.melody.common.transfer;

import java.lang.Thread.State;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferThread implements Runnable {

	private TransferMultiThread _transferMultiThread;
	private TransferableFileSystem _destinationFileSystem;

	private Thread _thread;
	private Throwable _finalError;

	protected TransferThread(TransferMultiThread p,
			TransferableFileSystem destFS, int index) {
		setTransferMultiThread(p);
		setDestinationFileSystem(destFS);

		setThread(new Thread(p.getThreadGroup(), this, p.getThreadGroup()
				.getName() + "-" + index));
		setFinalError(null);
	}

	protected short getFinalState() {
		if (getThread().getState() == State.NEW) {
			return TransferMultiThread.NEW;
		} else if (getThread().getState() != State.TERMINATED) {
			return TransferMultiThread.RUNNING;
		} else if (getFinalError() == null) {
			return TransferMultiThread.SUCCEED;
		} else if (getFinalError() instanceof MelodyException) {
			return TransferMultiThread.FAILED;
		} else if (getFinalError() instanceof InterruptedException) {
			return TransferMultiThread.INTERRUPTED;
		} else {
			return TransferMultiThread.CRITICAL;
		}
	}

	protected void startProcessing() {
		getThread().start();
	}

	protected void waitTillProcessingIsDone() throws InterruptedException {
		waitTillProcessingIsDone(0, 0);
	}

	protected void waitTillProcessingIsDone(long millis)
			throws InterruptedException {
		waitTillProcessingIsDone(millis, 0);
	}

	protected void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException {
		getThread().join(millis, nanos);
	}

	@Override
	public void run() {
		try {
			TransferMultiThread tmt = getTransferMultiThread();
			while (true) {
				Transferable t = tmt.getNextTransferable();
				if (t == null) {
					return;
				}
				tmt.transfer(getDestinationFileSystem(), t);
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			getDestinationFileSystem().release();
		}
	}

	private TransferMultiThread getTransferMultiThread() {
		return _transferMultiThread;
	}

	private void setTransferMultiThread(TransferMultiThread p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferMultiThread.class.getCanonicalName() + ".");
		}
		_transferMultiThread = p;
	}

	protected TransferableFileSystem getDestinationFileSystem() {
		return _destinationFileSystem;
	}

	private TransferableFileSystem setDestinationFileSystem(
			TransferableFileSystem destinationFileSystem) {
		if (destinationFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferableFileSystem.class.getCanonicalName() + ".");
		}
		TransferableFileSystem previous = getDestinationFileSystem();
		_destinationFileSystem = destinationFileSystem;
		return previous;
	}

	private Thread getThread() {
		return _thread;
	}

	private Thread setThread(Thread t) {
		return _thread = t;
	}

	protected Throwable getFinalError() {
		return _finalError;
	}

	private Throwable setFinalError(Throwable e) {
		return _finalError = e;
	}

}