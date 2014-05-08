package com.wat.cloud.libvirt;

import org.libvirt.ErrorCallback;
import org.libvirt.jna.virError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

/**
 * <P>
 * Trace libvirt error messages into the logging subsystem instead of stderr.
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtErrorCallback extends ErrorCallback {

	private static Logger log = LoggerFactory
			.getLogger(LibVirtErrorCallback.class);

	@Override
	public void errorCallback(Pointer userData, virError error) {
		log.trace(error.message);
	}

}