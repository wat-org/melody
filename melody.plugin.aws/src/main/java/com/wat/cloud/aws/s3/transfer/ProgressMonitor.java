package com.wat.cloud.aws.s3.transfer;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProgressMonitor implements ProgressListener {

	private static Logger log = LoggerFactory.getLogger(ProgressMonitor.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"mm:ss,SSS");

	private String _srcFS = null;
	private String _destFS = null;
	private String _src;
	private String _dest;
	private long _totalsize;
	private long _uploadedsize = 0;
	private int _uploadedpercent = 0;
	private long _starttime;

	/**
	 * @param srcFS
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no details concerning
	 *            the local file system will be printed.
	 * @param destFS
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no details concerning
	 *            the destination file system will be printed.
	 */
	public ProgressMonitor(String srcFS, String destFS, String src,
			String dest, long max) {
		_srcFS = srcFS;
		_destFS = destFS;
		_src = src;
		_dest = dest;
		_totalsize = max;
		_starttime = System.currentTimeMillis();
	}

	@Override
	public void progressChanged(ProgressEvent progressEvent) {
		StringBuilder str = new StringBuilder();
		if (_srcFS != null) {
			str.append(_srcFS);
			str.append(":");
		}
		str.append(_src);
		str.append(" -> ");
		if (_destFS != null) {
			str.append(_destFS);
			str.append(":");
		}
		str.append(_dest);

		switch (progressEvent.getEventCode()) {
		case ProgressEvent.CANCELED_EVENT_CODE:
			str.append(" (CANCELED)");
			break;
		case ProgressEvent.COMPLETED_EVENT_CODE:
			str.append(" (COMPLETED)");
			break;
		case ProgressEvent.FAILED_EVENT_CODE:
			str.append(" (FAILED)");
			break;
		case ProgressEvent.PART_COMPLETED_EVENT_CODE:
			str.append(" (PART_COMPLETED)");
			break;
		case ProgressEvent.PART_FAILED_EVENT_CODE:
			str.append(" (PART_FAILED)");
			break;
		case ProgressEvent.PART_STARTED_EVENT_CODE:
			str.append(" (PART_STARTED)");
			break;
		case ProgressEvent.PREPARING_EVENT_CODE:
			str.append(" (PREPARING)");
			break;
		case ProgressEvent.RESET_EVENT_CODE:
			str.append(" (RESET)");
			break;
		case ProgressEvent.STARTED_EVENT_CODE:
			str.append(" (STARTED)");
			break;
		case 0:
			// can be equal to 0, indicating a packet have been sent
			_uploadedsize += progressEvent.getBytesTransferred();
			int uploadedpercent = (int) (_uploadedsize * 100 / _totalsize);
			if (_uploadedpercent != uploadedpercent) {
				_uploadedpercent = uploadedpercent;
				str.append(" (");
				str.append(_uploadedpercent);
				str.append("% - ");
				str.append(_uploadedsize);
				str.append("/");
				str.append(_totalsize);
				str.append(" - ");
				str.append(sdf.format(System.currentTimeMillis() - _starttime));
				str.append(")");
			}
		}
		log.info(str.toString());
	}

}