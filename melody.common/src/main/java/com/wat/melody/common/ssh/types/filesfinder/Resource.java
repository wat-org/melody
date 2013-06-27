package com.wat.melody.common.ssh.types.filesfinder;

import java.nio.file.Path;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Resource {

	public Path getPath();

	public ResourceSpecification setResourceSpecification(
			ResourceSpecification spec);

}