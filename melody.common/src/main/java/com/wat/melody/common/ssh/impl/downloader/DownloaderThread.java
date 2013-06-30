package com.wat.melody.common.ssh.impl.downloader;

import java.lang.Thread.State;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ssh.filesfinder.RemoteResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderThread implements Runnable {

	private DownloaderMultiThread _download;
	private Thread _thread;
	private Throwable _finalError;

	protected DownloaderThread(DownloaderMultiThread p, int index) {
		setDownloader(p);
		setThread(new Thread(p.getThreadGroup(), this, p.getThreadGroup()
				.getName() + "-" + index));
		setFinalError(null);
	}

	protected short getFinalState() {
		if (getThread().getState() == State.NEW) {
			return DownloaderMultiThread.NEW;
		} else if (getThread().getState() != State.TERMINATED) {
			return DownloaderMultiThread.RUNNING;
		} else if (getFinalError() == null) {
			return DownloaderMultiThread.SUCCEED;
		} else if (getFinalError() instanceof MelodyException) {
			return DownloaderMultiThread.FAILED;
		} else if (getFinalError() instanceof InterruptedException) {
			return DownloaderMultiThread.INTERRUPTED;
		} else {
			return DownloaderMultiThread.CRITICAL;
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
			channel = getDownloader().getSession().openSftpChannel();
			while (true) {
				RemoteResource r = null;
				synchronized (getDownloader().getRemoteResources()) {
					if (getDownloader().getRemoteResources().size() == 0) {
						return;
					}
					r = getDownloader().getRemoteResources().remove(0);
				}
				getDownloader().download(channel, r);
			}
		} catch (Throwable Ex) {
			setFinalError(Ex);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
	}

	private DownloaderMultiThread getDownloader() {
		return _download;
	}

	private void setDownloader(DownloaderMultiThread p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DownloaderMultiThread.class.getCanonicalName() + ".");
		}
		_download = p;
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