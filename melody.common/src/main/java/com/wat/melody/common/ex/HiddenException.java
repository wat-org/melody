package com.wat.melody.common.ex;

/**
 * <p>
 * {@link HiddenException} have no message, only a cause.
 * </p>
 * <p>
 * When {@link MelodyException#getUserFriendlyStackTrace(Throwable)} encounter a
 * {@link HiddenException}, it will not print its cause (and inner causes).
 * </p>
 * <p>
 * When {@link MelodyException#getFullStackTrace(Throwable)} encounter a
 * {@link HiddenException}, it will print its cause (and inner causes).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class HiddenException extends MelodyException {

	private static final long serialVersionUID = -1184066155132415814L;

	public HiddenException(Throwable cause) {
		super((Throwable) cause);
	}

}