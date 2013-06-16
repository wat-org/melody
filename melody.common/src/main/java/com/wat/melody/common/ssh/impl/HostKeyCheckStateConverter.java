package com.wat.melody.common.ssh.impl;

import com.jcraft.jsch.HostKeyRepository;
import com.wat.melody.common.ssh.types.HostKeyCheckState;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class HostKeyCheckStateConverter {

	public static HostKeyCheckState convert(int jschHostKeyCheckState) {
		switch (jschHostKeyCheckState) {
		case HostKeyRepository.OK:
			return HostKeyCheckState.EQUALS;
		case HostKeyRepository.CHANGED:
			return HostKeyCheckState.CHANGED;
		case HostKeyRepository.NOT_INCLUDED:
			return HostKeyCheckState.NOT_INCLUDED;
		default:
			throw new IllegalArgumentException(jschHostKeyCheckState
					+ ": Not accepted. " + "Must be one of { 0, 1, 2 }.");
		}
	}

	public static int convert(HostKeyCheckState state) {
		switch (state) {
		case EQUALS:
			return HostKeyRepository.OK;
		case CHANGED:
			return HostKeyRepository.CHANGED;
		case NOT_INCLUDED:
			return HostKeyRepository.NOT_INCLUDED;
		default:
			throw new RuntimeException("BUG ! '" + state
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}

}