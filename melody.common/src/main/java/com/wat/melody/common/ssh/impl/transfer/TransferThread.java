package com.wat.melody.common.ssh.impl.transfer;

import java.lang.Thread.State;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ssh.filesfinder.Resource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferThread implements Runnable {

	private TransferMultiThread _transferMultiThread;
	private Thread _thread;
	private Throwable _finalError;

	protected TransferThread(TransferMultiThread p, int index) {
		setTransferMultiThread(p);
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
		// TODO : channel should be passed in constructor and stored in a member
		ChannelSftp channel = null;
		try {
			channel = getTransferMultiThread().getSession().openSftpChannel();
			while (true) {
				Resource r = getTransferMultiThread()
						.getNextResourceToTransfer();
				if (r == null) {
					return;
				}
				getTransferMultiThread()._transfer(channel, r);
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
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