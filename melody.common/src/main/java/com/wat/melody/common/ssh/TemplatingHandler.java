package com.wat.melody.common.ssh;

import java.nio.file.Path;

import com.wat.melody.common.ssh.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface TemplatingHandler {

	public Path doTemplate(Path source) throws TemplatingException;

}
