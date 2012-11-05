package com.wat.melody.common.utils;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public enum ReturnCode {

	OK(0), KO(1), INTERRUPTED(130), ERRGEN(255);

	private final int miValue;

	private ReturnCode(int v) {
		this.miValue = v;
	}

	public int getValue() {
		return miValue;
	}

}