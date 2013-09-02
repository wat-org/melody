package com.wat.melody.plugin.copy.common.types;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * <p>
 * A {@link CopyResourcesSpecification} is a {@link ResourcesSpecification}
 * which specifies a default src-basedir and dest-basedir. src-basedir and
 * dest-basedir are by default equals to the given basedir, and, when set, if
 * relative, they will be maked absolute, regarding the given basedir.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class CopyResourcesSpecification extends ResourcesSpecification {

	private Path _basedir;

	public CopyResourcesSpecification(File basedir) {
		super(basedir.toString(), basedir.toString());
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