package com.wat.melody.common.ssh.filesfinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 * A {@link LocalResourcesSpecification} is a {@link ResourcesSpecification}
 * which specifies the src-basedir and the dest-basedir. src-basedir is by
 * default equals to ".". dest-basedir is by default equals to the given
 * basedir, and if relative, it will be maked absolute, regarding the given
 * basedir.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteResourcesSpecification extends ResourcesSpecification {

	private Path _basedir;

	public RemoteResourcesSpecification(File basedir) {
		super(".", basedir.toString());
		_basedir = Paths.get(basedir.getAbsolutePath()).normalize();
	}

	@Override
	public String setDestBaseDir(String dir) {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		if (!new File(dir).isAbsolute()) {
			// make it absolute, regarding the basedir
			dir = _basedir.resolve(dir).normalize().toString();
		}
		return super.setDestBaseDir(dir);
	}

}