package com.wat.melody.common.ssh.filesfinder;

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