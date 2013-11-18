package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.Objects;

/**
 * <p>
 * A simple visitor of files with default behavior to visit all files and to
 * re-throw @{link IOException}.
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
public class EnhancedFileVisitor<T> implements FileVisitor<T> {

	/**
	 * Initializes a new instance of this class.
	 */
	protected EnhancedFileVisitor() {
	}

	/**
	 * Invoked for a directory before entries in the directory are visited.
	 * 
	 * <p>
	 * Unless overridden, this method returns {@link FileVisitResult#CONTINUE
	 * CONTINUE}.
	 */
	@Override
	public FileVisitResult preVisitDirectory(T dir, EnhancedFileAttributes attrs)
			throws IOException {
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
	public FileVisitResult visitFile(T file, EnhancedFileAttributes attrs)
			throws IOException {
		Objects.requireNonNull(file);
		Objects.requireNonNull(attrs);
		return FileVisitResult.CONTINUE;
	}

	/**
	 * Invoked for a file that could not be visited.
	 * 
	 * <p>
	 * Unless overridden, this method re-throws the {@link IOException} that
	 * prevented the file from being visited.
	 */
	@Override
	public FileVisitResult visitFileFailed(T file, IOException exc)
			throws IOException {
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
	 * otherwise this method re-throws the {@link IOException} that caused the
	 * iteration of the directory to terminate prematurely.
	 */
	@Override
	public FileVisitResult postVisitDirectory(T dir, IOException exc)
			throws IOException {
		Objects.requireNonNull(dir);
		if (exc != null)
			throw exc;
		return FileVisitResult.CONTINUE;
	}

}