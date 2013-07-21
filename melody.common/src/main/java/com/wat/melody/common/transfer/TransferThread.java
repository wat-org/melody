package com.wat.melody.common.transfer;

import java.lang.Thread.State;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.transfer.exception.TransferException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferThread implements Runnable {

	private TransferMultiThread _transferMultiThread;
	private FileSystem _sourceFileSystem;
	private FileSystem _destinationFileSystem;

	private Thread _thread;
	private Throwable _finalError;

	protected TransferThread(TransferMultiThread p, FileSystem srcFS,
			FileSystem destFS, int index) {
		setTransferMultiThread(p);
		setSourceFileSystem(srcFS);
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
			_run();
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			try {
				getSourceFileSystem().release();
			} finally {
				getDestinationFileSystem().release();
			}
		}
	}

	private void _run() throws TransferException, InterruptedException {
		while (true) {
			Transferable r = getTransferMultiThread().getNextTransferable();
			if (r == null) {
				return;
			}
			getTransferMultiThread()._transfer(getSourceFileSystem(),
					getDestinationFileSystem(), r);
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

	protected FileSystem getSourceFileSystem() {
		return _sourceFileSystem;
	}

	private FileSystem setSourceFileSystem(FileSystem sourceFileSystem) {
		if (sourceFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getSourceFileSystem();
		_sourceFileSystem = sourceFileSystem;
		return previous;
	}

	protected FileSystem getDestinationFileSystem() {
		return _destinationFileSystem;
	}

	private FileSystem setDestinationFileSystem(FileSystem destinationFileSystem) {
		if (destinationFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getDestinationFileSystem();
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