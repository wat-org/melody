package com.wat.melody.common.telnet.impl;

import java.io.IOException;
import java.io.OutputStream;

import com.wat.melody.common.telnet.impl.exception.UnexpectedResultReceived;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetResponseAnalyzer {

	public static String removeTrailingCrLf(String datas) {
		int len = datas.length();
		// remove last \n
		if (len > 0 && datas.charAt(len - 1) == '\n') {
			datas = datas.substring(0, len - 1);
		}
		len = datas.length();
		// remove last \r
		if (len > 0 && datas.charAt(len - 1) == '\r') {
			datas = datas.substring(0, len - 1);
		}
		return datas;
	}

	private StringBuilder _datas = new StringBuilder();
	private int _lastRead = 0;
	private int _lastAnalyzed = 0;

	public TelnetResponseAnalyzer() {
	}

	public synchronized void append(char c) {
		// TODO : throw an exception if mode console detected
		_datas.append(c);
	}

	/**
	 * @return <tt>null</tt> if no new data was read since last call.
	 */
	public synchronized Character readNext() {
		if (_datas.length() > _lastRead) {
			return _datas.charAt(_lastRead++);
		}
		return null;
	}

	public boolean analyze(OutputStream out, String startToTruncate,
			TelnetResponsesMatcher expected, TelnetResponsesMatcher unexpected)
			throws UnexpectedResultReceived, IOException {
		// examine the part of the datas that where not already analyzed
		String datas = _datas.substring(_lastAnalyzed, _lastRead);
		// if given, must remove startToTruncate to the beginning of the datas
		boolean truncated = (startToTruncate == null);
		if (startToTruncate != null && datas.indexOf(startToTruncate) == 0) {
			datas = datas.substring(startToTruncate.length());
			truncated = true;
		}
		// if given, try to found unexpected inside the datas
		String[] res = null;
		if (unexpected != null) {
			res = unexpected.matches(datas);
		}
		// if unexpected found, throw ex
		if (truncated && res != null) {
			_lastAnalyzed = _lastRead;
			if (out != null) {
				out.write(res[0].getBytes());
			}
			throw new UnexpectedResultReceived(res[1]);
		}
		// if expected found, return true
		res = expected.matches(datas);
		if (truncated && res != null) {
			_lastAnalyzed = _lastRead;
			if (out != null) {
				out.write(res[0].getBytes());
			}
			return true;
		}
		// if nothing found, return false
		return false;
	}

}