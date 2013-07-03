package com.wat.melody.common.ssh.impl;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.log.LogThreshold;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LoggerOutputStream extends OutputStream {

	private static Logger log = LoggerFactory
			.getLogger(LoggerOutputStream.class);

	protected StringBuffer _buffer;
	private String _prefix;
	private LogThreshold _level;

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
		return _buffer;
	}

	protected StringBuffer setBuffer(StringBuffer b) {
		if (b == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ StringBuffer.class.getCanonicalName() + ".");
		}
		StringBuffer previous = getBuffer();
		_buffer = b;
		return previous;
	}

	protected String getPrefix() {
		return _prefix;
	}

	protected String setPrefix(String sPrefix) {
		if (sPrefix == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getPrefix();
		_prefix = sPrefix;
		return previous;
	}

	protected LogThreshold getLevel() {
		return _level;
	}

	protected LogThreshold setLevel(LogThreshold level) {
		if (level == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ LogThreshold.class.getCanonicalName() + ".");
		}
		LogThreshold previous = getLevel();
		_level = level;
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
	 * simple workaround is to call close.
	 */
	@Override
	public void close() {
		if (getBuffer().length() != 0) {
			log();
			setBuffer(new StringBuffer());
		}
	}

	/**
	 * <p>
	 * Append the given byte to this object. If the byte if a '\r' or a '\n',
	 * all previously written bytes will be logged and this object will be
	 * restored to its initial state.
	 * </p>
	 * 
	 * <p>
	 * Note that when writing '\r' and just after '\n', this object will ignore
	 * the '\n'.
	 * </p>
	 * 
	 * @param b
	 *            is the byte to write into the buffer.
	 */
	@Override
	public void write(int b) {
		if (getLastChar() == (char) 13) {
			// when \r\n, just print \n
			deleteLastChar();
			log();
			setBuffer(new StringBuffer());
			if (b != 10) {
				getBuffer().append((char) b);
			}
		} else if (b == 10) {
			log();
			setBuffer(new StringBuffer());
		} else {
			getBuffer().append((char) b);
		}

	}

	public void log() {
		String msg = getBuffer().toString();
		// Removing all colorized stuff
		msg = msg.replaceAll("\\033\\[[0-9]+G", "");
		msg = msg.replaceAll("\\033\\[0;[0-9]+m", "");
		msg = getPrefix() + " " + msg;
		switch (getLevel()) {
		case ALL:
			log.trace(msg);
			break;
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
			log.error(msg);
			break;
		case OFF:
			break;
		}
	}

}