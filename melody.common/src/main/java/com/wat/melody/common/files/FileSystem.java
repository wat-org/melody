package com.wat.melody.common.files;

import java.io.IOException;
import java.io.InterruptedIOException;
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

import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.SymbolicLinkNotSupported;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface FileSystem {

	public void release();

	public boolean exists(Path path, LinkOption... options);

	public boolean exists(String path, LinkOption... options);

	public boolean isDirectory(Path path, LinkOption... options);

	public boolean isDirectory(String path, LinkOption... options);

	public boolean isRegularFile(Path path, LinkOption... options);

	public boolean isRegularFile(String path, LinkOption... options);

	public boolean isSymbolicLink(Path path);

	public boolean isSymbolicLink(String path);

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
	 *             if the given dir cannot be created because the parent element
	 *             doesn't exists or because the parent element exists but is
	 *             not a directory.
	 * @throws FileAlreadyExistsException
	 *             if the given dir cannot be created because a file with the
	 *             same name already exists but is not a directory.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IllegalFileAttributeException
	 *             if any file attribute is invalid. This exception can contains
	 *             multiple causes :
	 *             <ul>
	 *             <li>{@link UnsupportedOperationException} if the attribute
	 *             view is not available ;</li>
	 *             <li>{@link IllegalArgumentException} if the attribute name is
	 *             not specified, or is not recognized, or the attribute value
	 *             is of the correct type but has an inappropriate value ;</li>
	 *             <li> {@link ClassCastException} if the attribute value is not
	 *             of the expected type or is a collection containing elements
	 *             that are not of the expected type ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if the given directory cannot be created for any other
	 *             reason.
	 */
	public abstract void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException;

	public abstract void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException;

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
	 * @throws IllegalFileAttributeException
	 *             if any file attribute is invalid. This exception can contains
	 *             multiple causes :
	 *             <ul>
	 *             <li>{@link UnsupportedOperationException} if the attribute
	 *             view is not available ;</li>
	 *             <li>{@link IllegalArgumentException} if the attribute name is
	 *             not specified, or is not recognized, or the attribute value
	 *             is of the correct type but has an inappropriate value ;</li>
	 *             <li> {@link ClassCastException} if the attribute value is not
	 *             of the expected type or is a collection containing elements
	 *             that are not of the expected type ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if the given directories cannot be created for any other
	 *             reason.
	 */
	public abstract void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, IllegalFileAttributeException,
			FileAlreadyExistsException, AccessDeniedException;

	public abstract void createDirectories(String dir,
			FileAttribute<?>... attrs) throws IOException,
			IllegalFileAttributeException, FileAlreadyExistsException,
			AccessDeniedException;

	/**
	 * @param link
	 * @param target
	 * @param attrs
	 * 
	 * @throws SymbolicLinkNotSupported
	 *             if the file system doesn't support symbolic links.
	 * @throws NoSuchFileException
	 *             if a parent directory doesn't exists.
	 * @throws FileAlreadyExistsException
	 *             if the last element of the given link cannot be created
	 *             because a file with the same name already exists but is not a
	 *             link.
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IllegalFileAttributeException
	 *             if any file attribute is invalid. This exception can contains
	 *             multiple causes :
	 *             <ul>
	 *             <li>{@link UnsupportedOperationException} if the attribute
	 *             view is not available ;</li>
	 *             <li>{@link IllegalArgumentException} if the attribute name is
	 *             not specified, or is not recognized, or the attribute value
	 *             is of the correct type but has an inappropriate value ;</li>
	 *             <li> {@link ClassCastException} if the attribute value is not
	 *             of the expected type or is a collection containing elements
	 *             that are not of the expected type ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if the given link cannot be created for any other reason.
	 */
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException;

	public void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException;

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

	public Path readSymbolicLink(String link) throws IOException,
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

	public void delete(String path) throws IOException, NoSuchFileException,
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

	public boolean deleteIfExists(String path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException;

	/**
	 * <p>
	 * Delete a directory and all its content.
	 * </p>
	 * 
	 * <p>
	 * Will not throw {@link NoSuchFileException} if the given directory doesn't
	 * exists.
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

	public void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException;

	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException;

	public DirectoryStream<Path> newDirectoryStream(String path)
			throws IOException, InterruptedIOException, NoSuchFileException,
			NotDirectoryException, AccessDeniedException;

	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException;

	public EnhancedFileAttributes readAttributes(String path)
			throws IOException, NoSuchFileException, AccessDeniedException;

	/**
	 * @param path
	 * @param attributes
	 * 
	 * @throws NoSuchFileException
	 *             if the provided path is not a file.
	 * @throws IllegalFileAttributeException
	 *             if any file attribute is invalid. This exception can contains
	 *             multiple causes :
	 *             <ul>
	 *             <li>{@link UnsupportedOperationException} if the attribute
	 *             view is not available ;</li>
	 *             <li>{@link IllegalArgumentException} if the attribute name is
	 *             not specified, or is not recognized, or the attribute value
	 *             is of the correct type but has an inappropriate value ;</li>
	 *             <li> {@link ClassCastException} if the attribute value is not
	 *             of the expected type or is a collection containing elements
	 *             that are not of the expected type ;</li>
	 *             </ul>
	 * @throws AccessDeniedException
	 *             if a permission problem occurred.
	 * @throws IOException
	 *             if an I/O error occurred.
	 */
	public void setAttributes(Path path, FileAttribute<?>... attributes)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException;

	public void setAttributes(String path, FileAttribute<?>... attributes)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException;

}