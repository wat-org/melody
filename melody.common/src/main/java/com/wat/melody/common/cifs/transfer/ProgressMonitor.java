package com.wat.melody.common.cifs.transfer;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProgressMonitor {

	private static Logger log = LoggerFactory.getLogger(ProgressMonitor.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"mm:ss,SSS");

	private String _srcFS;
	private String _destFS;
	private String _src;
	private String _dest;
	private long _totalsize;
	private long _uploadedsize;
	private int _uploadedpercent;
	private long _starttime;

	/**
	 * @param srcFS
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no details concerning
	 *            the local file system will be printed.
	 * @param destFS
	 *            Can be <tt>null</tt>. If <tt>null</tt>, no details concerning
	 *            the destination file system will be printed.
	 */
	public ProgressMonitor(String srcFS, String destFS) {
		_srcFS = srcFS;
		_destFS = destFS;
	}

	public void init(int op, String src, String dest, long max) {
		_src = src;
		_dest = dest;
		_totalsize = max;
		_starttime = System.currentTimeMillis();
	}

	public boolean count(long count) {
		_uploadedsize += count;
		int uploadedpercent = (int) (_uploadedsize * 100 / _totalsize);
		if (_uploadedpercent != uploadedpercent) {
			_uploadedpercent = uploadedpercent;
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
			str.append(" (");
			str.append(_uploadedpercent);
			str.append("% - ");
			str.append(_uploadedsize);
			str.append("/");
			str.append(_totalsize);
			str.append(" - ");
			str.append(sdf.format(System.currentTimeMillis() - _starttime));
			str.append(")");
			log.info(str.toString());
		}
		return true;
	}

	public void end() {
		// nothing to do
	}

}