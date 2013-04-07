package com.wat.melody.common.keypair;

import java.io.File;
import java.nio.file.Path;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryException;

public class KeyPairRepositoryPath implements IFileBased {

	private String _path;

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepositoryPath(String sPath)
			throws KeyPairRepositoryException {
		_path = sPath;
		try {
			FS.validateDirPath(_path);
		} catch (IllegalDirectoryException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoPathEx_INVALID_REPO_PATH, _path), Ex);
		}
		// Create the directory if it doesn't exists
		File f = new File(_path);
		if (!f.exists() && !f.mkdirs()) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoPathEx_FAILED_TO_CREATE_REPO_PATH,
					_path));
		}
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepositoryPath(File path)
			throws KeyPairRepositoryException {
		this(path.toString());
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepositoryPath(Path path)
			throws KeyPairRepositoryException {
		this(path.toString());
	}

	public String getPath() {
		return _path;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof KeyPairRepositoryPath) {
			KeyPairRepositoryPath kprp = (KeyPairRepositoryPath) anObject;
			return getPath().equals(kprp.getPath());
		}
		return false;
	}

	@Override
	public String toString() {
		return getPath();
	}

}
