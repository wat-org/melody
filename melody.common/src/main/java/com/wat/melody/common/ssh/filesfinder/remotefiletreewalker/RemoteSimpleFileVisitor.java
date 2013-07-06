package com.wat.melody.common.ssh.filesfinder.remotefiletreewalker;

import java.nio.file.FileVisitResult;
import java.util.Objects;

import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.RemoteFileAttributes;

/**
 * <p>
 * A simple visitor of files with default behavior to visit all files and to
 * re-throw @{link SshSessionException}.
 * </p>
 * 
 * <p>
 * Methods in this class may be overridden subject to their general contract.
 * </p>
 * 
 * @param <T>
 *            The type of reference to the files
 * 
 * @since 1.7
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteSimpleFileVisitor<T> implements RemoteFileVisitor<T> {

	/**
	 * Initializes a new instance of this class.
	 */
	protected RemoteSimpleFileVisitor() {
	}

	/**
	 * Invoked for a directory before entries in the directory are visited.
	 * 
	 * <p>
	 * Unless overridden, this method returns {@link FileVisitResult#CONTINUE
	 * CONTINUE}.
	 */
	@Override
	public FileVisitResult preVisitDirectory(T dir, RemoteFileAttributes attrs)
			throws SshSessionException {
		Objects.requireNonNull(dir);
		Objects.requireNonNull(attrs);
		return FileVisitResult.CONTINUE;
	}

	/**
	 * Invoked for a file in a directory.
	 * 
	 * <p>
	 * Unless overridden, this method returns {@link FileVisitResult#CONTINUE
	 * CONTINUE}.
	 */
	@Override
	public FileVisitResult visitFile(T file, RemoteFileAttributes attrs)
			throws SshSessionException {
		Objects.requireNonNull(file);
		Objects.requireNonNull(attrs);
		return FileVisitResult.CONTINUE;
	}

	/**
	 * Invoked for a file that could not be visited.
	 * 
	 * <p>
	 * Unless overridden, this method re-throws the {@link SshSessionException}
	 * that prevented the file from being visited.
	 */
	@Override
	public FileVisitResult visitFileFailed(T file, SshSessionException exc)
			throws SshSessionException {
		Objects.requireNonNull(file);
		throw exc;
	}

	/**
	 * Invoked for a directory after entries in the directory, and all of their
	 * descendants, have been visited.
	 * 
	 * <p>
	 * Unless overridden, this method returns {@link FileVisitResult#CONTINUE
	 * CONTINUE} if the directory iteration completes without an I/O exception;
	 * otherwise this method re-throws the {@link SshSessionException} that
	 * caused the iteration of the directory to terminate prematurely.
	 */
	@Override
	public FileVisitResult postVisitDirectory(T dir, SshSessionException exc)
			throws SshSessionException {
		Objects.requireNonNull(dir);
		if (exc != null)
			throw exc;
		return FileVisitResult.CONTINUE;
	}

}