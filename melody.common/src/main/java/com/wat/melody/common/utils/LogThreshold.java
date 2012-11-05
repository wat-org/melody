package com.wat.melody.common.utils;

import java.util.Arrays;

import com.wat.melody.common.utils.exception.IllegalLogThresholdException;

/**
 * <p>
 * This Enumeration represents a unified Log threshold. Useful to convert and
 * manipulate {@link org.apache.log4j.Level} and {@link java.util.loggin.Level}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public enum LogThreshold {
	ALL("ALL"), TRACE("TRACE"), DEBUG("DEBUG"), INFO("INFO"), WARNING("WARNING"), ERROR(
			"ERROR"), FATAL("FATAL"), OFF("OFF");

	/**
	 * <p>
	 * Converts the given value to a <code>LogThreshold</code>.
	 * </p>
	 * 
	 * @param v
	 *            is the value to convert.
	 * 
	 * @return the corresponding <code>LogThreshold</code> Enumeration Constant. <BR/>
	 *         More formally, this method will return : <BR/>
	 *         {@link LogThreshold#ALL} if the input value is equal to "ALL"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#TRACE} if the input value is equal to "TRACE"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#DEBUG} if the input value is equal to "DEBUG"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#INFO} if the input value is equal to "INFO"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#WARNING} if the input value is equal to
	 *         "WARNING" (case insensitive). <BR/>
	 *         {@link LogThreshold#ERROR} if the input value is equal to "ERROR"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#FATAL} if the input value is equal to "FATAL"
	 *         (case insensitive). <BR/>
	 *         {@link LogThreshold#OFF} if the input value is equal to "OFF"
	 *         (case insensitive). <BR/>
	 * 
	 * @throws IllegalLogThresholdException
	 *             if the given value is null.
	 * 
	 * @throws IllegalLogThresholdException
	 *             if the given value doesn't match any of the
	 *             <code>LogThreshold</code> Enumeration Constant.
	 * 
	 */
	public static LogThreshold parseString(String v)
			throws IllegalLogThresholdException {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ LogThreshold.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(LogThreshold.values()) + " ).");
		}
		if (v.trim().length() == 0) {
			throw new IllegalLogThresholdException(Messages.bind(
					Messages.LogThresholdEx_EMPTY_STRSTR, v));
		}
		for (LogThreshold lt : LogThreshold.class.getEnumConstants()) {
			if (lt.getValue().equalsIgnoreCase(v)) {
				return lt;
			}
		}
		throw new IllegalLogThresholdException(Messages.bind(
				Messages.LogThresholdEx_INVALID_STRSTR, v,
				Arrays.asList(LogThreshold.values())));
	}

	public static org.apache.log4j.Level convertToLog4jLevel(LogThreshold lt) {
		switch (lt) {
		case ALL:
			return org.apache.log4j.Level.ALL;
		case TRACE:
			return org.apache.log4j.Level.TRACE;
		case DEBUG:
			return org.apache.log4j.Level.DEBUG;
		case INFO:
			return org.apache.log4j.Level.INFO;
		case WARNING:
			return org.apache.log4j.Level.WARN;
		case ERROR:
			return org.apache.log4j.Level.ERROR;
		case FATAL:
			return org.apache.log4j.Level.FATAL;
		default:
			return org.apache.log4j.Level.OFF;
		}
	}

	public static java.util.logging.Level convertToJavaLoggingLevel(
			LogThreshold lt) {
		switch (lt) {
		case ALL:
			return java.util.logging.Level.ALL;
		case TRACE:
			return java.util.logging.Level.FINEST;
		case DEBUG:
			return java.util.logging.Level.FINE;
		case INFO:
			return java.util.logging.Level.INFO;
		case WARNING:
			return java.util.logging.Level.WARNING;
		case ERROR:
			return java.util.logging.Level.SEVERE;
		case FATAL:
			return java.util.logging.Level.SEVERE;
		default:
			return java.util.logging.Level.OFF;
		}
	}

	/**
	 * <p>
	 * Increase the given <code>LogThreshold</code>.
	 * </p>
	 * 
	 * @param lt
	 *            is the value to increase.
	 * 
	 * @return the increased <code>LogThreshold</code>. <BR/>
	 *         More formally, this method will return : <BR/>
	 *         {@link LogThreshold#TRACE} if the input value is equal to
	 *         {@link LogThreshold#ALL}. <BR/>
	 *         {@link LogThreshold#DEBUG} if the input value is equal to
	 *         {@link LogThreshold#TRACE}. <BR/>
	 *         {@link LogThreshold#INFO} if the input value is equal to
	 *         {@link LogThreshold#DEBUG}. <BR/>
	 *         {@link LogThreshold#WARNING} if the input value is equal to
	 *         {@link LogThreshold#INFO}. <BR/>
	 *         {@link LogThreshold#ERROR} if the input value is equal to
	 *         {@link LogThreshold#WARNING}. <BR/>
	 *         {@link LogThreshold#FATAL} if the input value is equal to
	 *         {@link LogThreshold#ERROR}. <BR/>
	 *         {@link LogThreshold#OFF} if the input value is equal to
	 *         {@link LogThreshold#FATAL}. <BR/>
	 * 
	 * @throws IllegalLogThresholdException
	 *             if the given value is already equals to the maximum (e.g.
	 *             {@link LogThreshold#OFF}).
	 * 
	 */
	public static LogThreshold increase(LogThreshold lt)
			throws IllegalLogThresholdException {
		switch (lt) {
		case ALL:
			return TRACE;
		case TRACE:
			return DEBUG;
		case DEBUG:
			return INFO;
		case INFO:
			return WARNING;
		case WARNING:
			return ERROR;
		case ERROR:
			return FATAL;
		case FATAL:
			return OFF;
		default:
			throw new IllegalLogThresholdException(Messages.bind(
					Messages.LogThresholdEx_MAX_REACHED, lt, OFF));
		}
	}

	/**
	 * <p>
	 * Decrease the given <code>LogThreshold</code>.
	 * </p>
	 * 
	 * @param lt
	 *            is the value to decrease.
	 * 
	 * @return the decreased <code>LogThreshold</code>. <BR/>
	 *         More formally, this method will return : <BR/>
	 *         {@link LogThreshold#ALL} if the input value is equal to
	 *         {@link LogThreshold#TRACE}. <BR/>
	 *         {@link LogThreshold#TRACE} if the input value is equal to
	 *         {@link LogThreshold#DEBUG}. <BR/>
	 *         {@link LogThreshold#DEBUG} if the input value is equal to
	 *         {@link LogThreshold#INFO}. <BR/>
	 *         {@link LogThreshold#INFO} if the input value is equal to
	 *         {@link LogThreshold#WARNING}. <BR/>
	 *         {@link LogThreshold#WARNING} if the input value is equal to
	 *         {@link LogThreshold#ERROR}. <BR/>
	 *         {@link LogThreshold#ERROR} if the input value is equal to
	 *         {@link LogThreshold#FATAL}. <BR/>
	 *         {@link LogThreshold#FATAL} if the input value is equal to
	 *         {@link LogThreshold#OFF}. <BR/>
	 * 
	 * @throws IllegalLogThresholdException
	 *             if the given value is already equals to the minimum (e.g.
	 *             {@link LogThreshold#ALL}).
	 * 
	 */
	public static LogThreshold decrease(LogThreshold lt)
			throws IllegalLogThresholdException {
		switch (lt) {
		case TRACE:
			return ALL;
		case DEBUG:
			return TRACE;
		case INFO:
			return DEBUG;
		case WARNING:
			return INFO;
		case ERROR:
			return WARNING;
		case FATAL:
			return ERROR;
		case OFF:
			return FATAL;
		default:
			throw new IllegalLogThresholdException(Messages.bind(
					Messages.LogThresholdEx_MIN_REACHED, lt, ALL));
		}
	}

	public static LogThreshold convertFromLog4jLevel(
			org.apache.log4j.Level level) {
		if (level.equals(org.apache.log4j.Level.ALL)) {
			return ALL;
		} else if (level.equals(org.apache.log4j.Level.TRACE)) {
			return TRACE;
		} else if (level.equals(org.apache.log4j.Level.DEBUG)) {
			return DEBUG;
		} else if (level.equals(org.apache.log4j.Level.INFO)) {
			return INFO;
		} else if (level.equals(org.apache.log4j.Level.WARN)) {
			return WARNING;
		} else if (level.equals(org.apache.log4j.Level.ERROR)) {
			return ERROR;
		} else if (level.equals(org.apache.log4j.Level.FATAL)) {
			return FATAL;
		} else {
			return OFF;
		}
	}

	public static org.apache.log4j.Level increase(org.apache.log4j.Level level)
			throws IllegalLogThresholdException {
		return convertFromLog4jLevel(level).increase().convertToLog4jLevel();
	}

	public static org.apache.log4j.Level decrease(org.apache.log4j.Level level)
			throws IllegalLogThresholdException {
		return convertFromLog4jLevel(level).decrease().convertToLog4jLevel();
	}

	public static LogThreshold convertFromJavaLoggingLevel(
			java.util.logging.Level level) {
		if (level.equals(java.util.logging.Level.ALL)) {
			return ALL;
		} else if (level.equals(java.util.logging.Level.FINEST)) {
			return TRACE;
		} else if (level.equals(java.util.logging.Level.FINER)) {
			return DEBUG;
		} else if (level.equals(java.util.logging.Level.FINE)) {
			return DEBUG;
		} else if (level.equals(java.util.logging.Level.CONFIG)) {
			return INFO;
		} else if (level.equals(java.util.logging.Level.INFO)) {
			return INFO;
		} else if (level.equals(java.util.logging.Level.WARNING)) {
			return WARNING;
		} else if (level.equals(java.util.logging.Level.SEVERE)) {
			return ERROR;
		} else {
			return OFF;
		}
	}

	public static java.util.logging.Level increase(java.util.logging.Level level)
			throws IllegalLogThresholdException {
		return convertFromJavaLoggingLevel(level).increase()
				.convertToJavaLoggingLevel();
	}

	public static java.util.logging.Level decrease(java.util.logging.Level level)
			throws IllegalLogThresholdException {
		return convertFromJavaLoggingLevel(level).decrease()
				.convertToJavaLoggingLevel();
	}

	private final String msValue;

	private LogThreshold(String v) {
		this.msValue = v;
	}

	public String getValue() {
		return this.msValue;
	}

	public LogThreshold increase() throws IllegalLogThresholdException {
		return increase(this);
	}

	public LogThreshold decrease() throws IllegalLogThresholdException {
		return decrease(this);
	}

	public org.apache.log4j.Level convertToLog4jLevel() {
		return convertToLog4jLevel(this);
	}

	public java.util.logging.Level convertToJavaLoggingLevel() {
		return convertToJavaLoggingLevel(this);
	}

}