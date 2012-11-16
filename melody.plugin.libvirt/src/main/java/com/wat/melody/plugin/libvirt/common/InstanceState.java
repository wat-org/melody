package com.wat.melody.plugin.libvirt.common;

import org.libvirt.DomainInfo;

import com.wat.melody.plugin.libvirt.common.exception.IllegalInstanceStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum InstanceState {

	RUNNING(DomainInfo.DomainState.VIR_DOMAIN_RUNNING), STOPPING(
			DomainInfo.DomainState.VIR_DOMAIN_SHUTDOWN), STOPPED(
			DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF);
	/*
	 * TODO : créer un state PENDING, qui correspond au moment ou libVitCloud
	 * provisionne le Domain
	 */
	/*
	 * TODO : créer un state SHUTTING_DOWN, qui correspond au moment ou
	 * libVitCloud dé-provisionne le Domain
	 */
	/**
	 * <p>
	 * Convert the given {@link DomainInfo.DomainState} to a
	 * {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given {@link DomainInfo.DomainState} to convert.
	 * 
	 * @return an {@link InstanceState} object, whose equal to the given input
	 *         <code>int</code>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input {@link DomainInfo.DomainState} is not a
	 *             valid {@link InstanceState} Enumeration Constant.
	 */
	public static InstanceState parseDomainState(DomainInfo.DomainState iState)
			throws IllegalInstanceStateException {
		for (InstanceState c : InstanceState.class.getEnumConstants()) {
			if (c.getState() == iState) {
				return c;
			}
		}
		throw new IllegalInstanceStateException(Messages.bind(
				Messages.InstanceStateEx_INVALID, iState,
				InstanceState.class.getCanonicalName()));
	}

	private final DomainInfo.DomainState miState;

	private InstanceState(DomainInfo.DomainState v) {
		this.miState = v;
	}

	private DomainInfo.DomainState getState() {
		return this.miState;
	}

}
