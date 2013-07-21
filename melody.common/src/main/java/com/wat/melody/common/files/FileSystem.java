package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;


/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface FileSystem {

	public void release();

	public boolean exists(Path path, LinkOption... options);

	public boolean isDirectory(Path path, LinkOption... options);

	public boolean isRegularFile(Path path, LinkOption... options);

	public boolean isSymbolicLink(Path path);

	/**
	 * 
	 * <p>
	 * Create the given directory. Will not fail if the given directory already
	 * exists.
	 * </p>
	 * 
	 * @param dir
	 * @param attrs
	 * 
	 * @throws NoSuchFileException
	 *             if a parent directory doesn't exists.
	 * @throws FileAlreadyExistsException
	 *             if the given dir cannot be created because a file with the
	 *             same name already exists but is not a directory.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given directory cannot be created for any other
	 *             reason.
	 */
	public abstract void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException;

	/**
	 * <p>
	 * Create the given directory tree. Will not fail if a path element of the
	 * given directory already exists.
	 * </p>
	 * 
	 * @param dir
	 * @param attrs
	 * 
	 * @throws FileAlreadyExistsException
	 *             if a path element of the given directory cannot be created
	 *             because a file with the same name already exists but is not a
	 *             directory.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given directories cannot be created for any other
	 *             reason.
	 */
	public abstract void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException;

	/**
	 * @param link
	 * @param target
	 * @param attrs
	 * 
	 * @throws NoSuchFileException
	 *             if a parent directory doesn't exists.
	 * @throws FileAlreadyExistsException
	 *             if the last element of the given link cannot be created
	 *             because a file with the same name already exists but is not a
	 *             link.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given link cannot be created for any other reason.
	 */
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException;

	/**
	 * @param link
	 * 
	 * @throws NoSuchFileException
	 *             if the provided link is not a file.
	 * @throws NotLinkException
	 *             if the provided link is a file but is not a link.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given link cannot be read for any other reason.
	 */
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException;

	/**
	 * <p>
	 * Delete file (a regular file, a directory or a link). If the given path
	 * denotes a link, the link is deleted, not its target.
	 * </p>
	 * 
	 * @param path
	 * 
	 * @throws NoSuchFileException
	 *             if the provided path is not a file.
	 * @throws DirectoryNotEmptyException
	 *             if the provided path is a directory with is not empty.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given path cannot be deleted for any other reason.
	 */
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException;

	/**
	 * <p>
	 * Delete file (a regular file, a directory or a link). If the given path
	 * denotes a link, the link is deleted, not its target.
	 * </p>
	 * 
	 * @param path
	 * 
	 * @throws DirectoryNotEmptyException
	 *             if the provided path is a directory with is not empty.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if the given path cannot be deleted for any other reason.
	 */
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException;

	/**
	 * <p>
	 * Delete a directory and all its content.
	 * </p>
	 * 
	 * @param dir
	 * 
	 * @throws NotDirectoryException
	 *             if the given path is not a directory.
	 * @throws IOException
	 *             if the given directory cannot be deleted.
	 */
	public void deleteDirectory(Path dir) throws IOException,
			NotDirectoryException, AccessDeniedException;

	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException;

	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NoSuchFileException, NotDirectoryException,
			AccessDeniedException;

}