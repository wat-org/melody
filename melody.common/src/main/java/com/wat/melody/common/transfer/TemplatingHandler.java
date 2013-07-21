package com.wat.melody.common.transfer;

import java.nio.file.Path;

import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface TemplatingHandler {

	public Path doTemplate(Path source) throws TemplatingException;

}