package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.common.utils.LogThreshold;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LoggerOutputStream extends OutputStream {

	private static Log log = LogFactory.getLog(LoggerOutputStream.class);

	protected StringBuffer mBuffer;
	private String msPrefix;
	private LogThreshold moLevel;

	public LoggerOutputStream(String sPrefix, LogThreshold level) {
		setBuffer(new StringBuffer());
		setPrefix(sPrefix);
		setLevel(level);
	}

	public LoggerOutputStream() {
		this(LogThreshold.INFO);
	}

	public LoggerOutputStream(String sPrefix) {
		this(sPrefix, LogThreshold.INFO);
	}

	public LoggerOutputStream(LogThreshold level) {
		this("", level);
	}

	protected StringBuffer getBuffer() {
		return mBuffer;
	}

	protected StringBuffer setBuffer(StringBuffer b) {
		if (b == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid StringBuffer.");
		}
		StringBuffer previous = getBuffer();
		mBuffer = b;
		return previous;
	}

	protected String getPrefix() {
		return msPrefix;
	}

	protected String setPrefix(String sPrefix) {
		if (sPrefix == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String.");
		}
		String previous = getPrefix();
		msPrefix = sPrefix;
		return previous;
	}

	protected LogThreshold getLevel() {
		return moLevel;
	}

	protected LogThreshold setLevel(LogThreshold level) {
		if (level == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid LogThreshold.");
		}
		LogThreshold previous = getLevel();
		moLevel = level;
		return previous;
	}

	private char getLastChar() {
		try {
			return getBuffer().charAt(getBuffer().length() - 1);
		} catch (IndexOutOfBoundsException Ex) {
			return (char) 0;
		}
	}

	private void deleteLastChar() {
		try {
			getBuffer().deleteCharAt(getBuffer().length() - 1);
		} catch (IndexOutOfBoundsException Ex) {
		}
	}

	/**
	 * This object doesn't write the last line if it doesn't end with '\n'. A
	 * simple workaround is to call flush.
	 */
	@Override
	public void flush() {
		if (getBuffer().length() != 0) {
			write();
			setBuffer(new StringBuffer());
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (getLastChar() == (char) 13) {
			// when \r\n, just print \n
			deleteLastChar();
			write();
			setBuffer(new StringBuffer());
			if (b != 10) {
				getBuffer().append((char) b);
			}
		} else if (b == 10) {
			write();
			setBuffer(new StringBuffer());
		} else {
			getBuffer().append((char) b);
		}

	}

	public void write() {
		String msg = getBuffer().toString();
		// Removing all colorized stuff
		msg = msg.replaceAll("\\033\\[[0-9]+G", "");
		msg = msg.replaceAll("\\033\\[0;[0-9]+m", "");
		msg = getPrefix() + " " + msg;
		switch (getLevel()) {
		case TRACE:
			log.trace(msg);
			break;
		case DEBUG:
			log.debug(msg);
			break;
		case INFO:
			log.info(msg);
			break;
		case WARNING:
			log.warn(msg);
			break;
		case ERROR:
			log.error(msg);
			break;
		case FATAL:
			log.fatal(msg);
			break;
		}
	}

}