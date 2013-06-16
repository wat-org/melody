package com.wat.melody.common.ssh.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.KnownHostsRepositoryPathException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsRepositoryPath implements IFileBased {

	private String _path;

	/**
	 * 
	 * @param path
	 * 
	 * @throws KnownHostsRepositoryPathException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws IllegalArgumentException
	 *             if the given path is <code>null</code>.
	 */
	public KnownHostsRepositoryPath(String sPath)
			throws KnownHostsRepositoryPathException {
		_path = sPath;
		try {
			FS.validateFilePath(_path);
		} catch (IllegalFileException | IllegalDirectoryException Ex) {
			throw new KnownHostsRepositoryPathException(Msg.bind(
					Messages.KnownHostsRepoPathEx_INVALID_REPO_PATH, _path), Ex);
		}
		// Create the file if it doesn't exists
		try {
			new File(_path).createNewFile();
		} catch (IOException Ex) {
			throw new KnownHostsRepositoryPathException(Msg.bind(
					Messages.KnownHostsRepoPathEx_FAILED_TO_CREATE_REPO, _path));
		}
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KnownHostsRepositoryPathException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KnownHostsRepositoryPath(File path)
			throws KnownHostsRepositoryPathException {
		this(path.toString());
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KnownHostsRepositoryPathException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KnownHostsRepositoryPath(Path path)
			throws KnownHostsRepositoryPathException {
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
		if (anObject instanceof KnownHostsRepositoryPath) {
			KnownHostsRepositoryPath kprp = (KnownHostsRepositoryPath) anObject;
			return getPath().equals(kprp.getPath());
		}
		return false;
	}

	@Override
	public String toString() {
		return getPath();
	}

}