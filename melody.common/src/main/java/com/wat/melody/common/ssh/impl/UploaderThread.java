package com.wat.melody.common.ssh.impl;

import java.lang.Thread.State;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ssh.types.SimpleResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderThread implements Runnable {

	private UploaderMultiThread moUpload;
	private Thread moThread;
	private Throwable moFinalError;

	protected UploaderThread(UploaderMultiThread p, int index) {
		setUploader(p);
		setThread(new Thread(p.getThreadGroup(), this, p.getThreadGroup()
				.getName() + "-" + index));
		setFinalError(null);
	}

	protected short getFinalState() {
		if (getThread().getState() == State.NEW) {
			return UploaderMultiThread.NEW;
		} else if (getThread().getState() != State.TERMINATED) {
			return UploaderMultiThread.RUNNING;
		} else if (getFinalError() == null) {
			return UploaderMultiThread.SUCCEED;
		} else if (getFinalError() instanceof MelodyException) {
			return UploaderMultiThread.FAILED;
		} else if (getFinalError() instanceof InterruptedException) {
			return UploaderMultiThread.INTERRUPTED;
		} else {
			return UploaderMultiThread.CRITICAL;
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
		ChannelSftp channel = null;
		try {
			channel = getUploader().getSession().openSftpChannel();
			while (true) {
				SimpleResource r = null;
				synchronized (getUploader().getSimpleResourcesList()) {
					if (getUploader().getSimpleResourcesList().size() == 0) {
						return;
					}
					r = getUploader().getSimpleResourcesList().remove(0);
				}
				getUploader().upload(channel, r);
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
	}

	private UploaderMultiThread getUploader() {
		return moUpload;
	}

	private void setUploader(UploaderMultiThread p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ UploaderMultiThread.class.getCanonicalName() + ".");
		}
		moUpload = p;
	}

	private Thread getThread() {
		return moThread;
	}

	private Thread setThread(Thread t) {
		return moThread = t;
	}

	protected Throwable getFinalError() {
		return moFinalError;
	}

	private Throwable setFinalError(Throwable e) {
		return moFinalError = e;
	}
}
