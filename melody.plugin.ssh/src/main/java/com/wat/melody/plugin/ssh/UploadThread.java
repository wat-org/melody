package com.wat.melody.plugin.ssh;

import java.lang.Thread.State;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.typedef.SimpleResource;
import com.wat.melody.common.utils.exception.MelodyException;
import com.wat.melody.plugin.ssh.common.exception.SshException;

public class UploadThread implements Runnable {

	private Upload moUpload;
	private Thread moThread;
	private Throwable moFinalError;

	public UploadThread(Upload p, int index) {
		setUpload(p);
		setThread(new Thread(p.getThreadGroup(), this, p.getThreadGroup()
				.getName() + "-" + index));
		initFinalError();
	}

	private void initFinalError() {
		moFinalError = null;
	}

	public short getFinalState() {
		if (getThread().getState() == State.NEW) {
			return Upload.NEW;
		} else if (getThread().getState() != State.TERMINATED) {
			return Upload.RUNNING;
		} else if (getFinalError() == null) {
			return Upload.SUCCEED;
		} else if (getFinalError() instanceof MelodyException) {
			return Upload.FAILED;
		} else if (getFinalError() instanceof InterruptedException) {
			return Upload.INTERRUPTED;
		} else {
			return Upload.CRITICAL;
		}
	}

	public void startProcessing() {
		getThread().start();
	}

	public void waitTillProcessingIsDone() throws InterruptedException {
		waitTillProcessingIsDone(0, 0);
	}

	public void waitTillProcessingIsDone(long millis)
			throws InterruptedException {
		waitTillProcessingIsDone(millis, 0);
	}

	public void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException {
		getThread().join(millis, nanos);
	}

	@Override
	public void run() {
		ChannelSftp channel = null;
		try {
			channel = getUpload().openSftpChannel(getUpload().getSession());
			while (true) {
				SimpleResource r = null;
				synchronized (getUpload().getSimpleResourcesList()) {
					if (getUpload().getSimpleResourcesList().size() == 0) {
						return;
					}
					r = getUpload().getSimpleResourcesList().remove(0);
				}
				try {
					getUpload().upload(channel, r);
				} catch (SshException Ex) {
					/*
					 * add the exception the Upload's exception list and goes on
					 * uploading another resource.
					 */
					getUpload().markState(Upload.FAILED);
					getUpload().getExceptionsList().add(Ex);
				}
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
	}

	private Upload getUpload() {
		return moUpload;
	}

	private void setUpload(Upload p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Upload.");
		}
		moUpload = p;
	}

	private Thread getThread() {
		return moThread;
	}

	private Thread setThread(Thread t) {
		return moThread = t;
	}

	public Throwable getFinalError() {
		return moFinalError;
	}

	private Throwable setFinalError(Throwable e) {
		return moFinalError = e;
	}
}
