package com.wat.melody.common.keypair;

import java.io.File;
import java.nio.file.Path;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryPathException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepositoryPath implements IFileBased {

	private String _path;

	/**
	 * @param path
	 *            is a path.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws KeyPairRepositoryPathException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             directory ;</li>
	 *             <li>if the creation of given path failed ;</li>
	 *             </ul>
	 */
	public KeyPairRepositoryPath(String path)
			throws KeyPairRepositoryPathException {
		_path = path;
		try {
			FS.validateDirPath(_path);
		} catch (IllegalDirectoryException Ex) {
			throw new KeyPairRepositoryPathException(Msg.bind(
					Messages.KeyPairRepoPathEx_INVALID_REPO_PATH, _path), Ex);
		}
		// Create the directory if it doesn't exists
		File f = new File(_path);
		if (!f.exists() && !f.mkdirs()) {
			throw new KeyPairRepositoryPathException(Msg.bind(
					Messages.KeyPairRepoPathEx_FAILED_TO_CREATE_REPO, _path));
		}
	}

	/**
	 * @param path
	 *            is a path.
	 * 
	 * @throws NullPointerException
	 *             if the given path is <tt>null</tt>.
	 * @throws KeyPairRepositoryPathException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             directory ;</li>
	 *             <li>if the creation of given path failed ;</li>
	 *             </ul>
	 */
	public KeyPairRepositoryPath(File path)
			throws KeyPairRepositoryPathException {
		this(path.toString());
	}

	/**
	 * @param path
	 *            is a path.
	 * 
	 * @throws NullPointerException
	 *             if the given path is <tt>null</tt>.
	 * @throws KeyPairRepositoryPathException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             directory ;</li>
	 *             <li>if the creation of given path failed ;</li>
	 *             </ul>
	 */
	public KeyPairRepositoryPath(Path path)
			throws KeyPairRepositoryPathException {
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