package com.wat.melody.plugin.ssh.common.types;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * <p>
 * A {@link LocalResourcesSpecification} is a {@link ResourcesSpecification}
 * which specifies a default src-basedir and dest-basedir. src-basedir is by
 * default equals to the given basedir, and, when set, if relative, it will be
 * maked absolute, regarding the given basedir. dest-basedir is by default
 * equals to ".".
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalResourcesSpecification extends ResourcesSpecification {

	private Path _basedir;

	public LocalResourcesSpecification(File basedir) {
		super(basedir.toString(), ".");
		if (!basedir.isAbsolute()) {
			throw new IllegalArgumentException("'" + basedir
					+ "': Not accepted. " + "Must be an absolute path.");
		}
		_basedir = Paths.get(basedir.toString());
	}

	@Override
	public String setSrcBaseDir(String dir) {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		if (!new File(dir).isAbsolute()) {
			// make it absolute, regarding the basedir
			dir = _basedir.resolve(dir).normalize().toString();
		}
		return super.setSrcBaseDir(dir);
	}

}