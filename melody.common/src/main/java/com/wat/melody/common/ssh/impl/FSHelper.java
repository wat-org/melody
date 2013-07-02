package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.SshSessionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FSHelper {

	public static void ln(Path link, Path target) throws SshSessionException {
		try {
			Files.createSymbolicLink(link, target);
		} catch (IOException Ex) {
			throw new SshSessionException(Msg.bind(Messages.FSEx_LN, link,
					target), Ex);
		}
	}

	public static void mkdir(Path dir) throws SshSessionException {
		try {
			Files.createDirectory(dir);
		} catch (IOException Ex) {
			throw new SshSessionException(Msg.bind(Messages.FSEx_MKDIR, dir),
					Ex);
		}
	}

}