package com.wat.melody.plugin.ssh.common;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.common.utils.LogThreshold;

public class LoggerOutputStream extends OutputStream {

	private static Log log = LogFactory.getLog(LoggerOutputStream.class);

	protected StringBuffer mBuffer;
	private String msPrefix;
	private LogThreshold moLevel;

	protected LoggerOutputStream(String sPrefix, LogThreshold level) {
		setBuffer(new StringBuffer());
		setPrefix(sPrefix);
		setLevel(level);
	}

	protected LoggerOutputStream() {
		this(LogThreshold.INFO);
	}

	protected LoggerOutputStream(String sPrefix) {
		this(sPrefix, LogThreshold.INFO);
	}

	protected LoggerOutputStream(LogThreshold level) {
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

	@Override
	public void write(int b) throws IOException {
		if (b == 10) {
			writeLine();
			setBuffer(new StringBuffer());
		} else {
			getBuffer().append((char) b);
		}

	}

	public void writeLine() {
		switch (getLevel()) {
		case TRACE:
			log.trace(getPrefix() + " " + getBuffer());
			break;
		case DEBUG:
			log.debug(getPrefix() + " " + getBuffer());
			break;
		case INFO:
			log.info(getPrefix() + " " + getBuffer());
			break;
		case WARNING:
			log.warn(getPrefix() + " " + getBuffer());
			break;
		case ERROR:
			log.error(getPrefix() + " " + getBuffer());
			break;
		case FATAL:
			log.fatal(getPrefix() + " " + getBuffer());
			break;
		}
	}

}
