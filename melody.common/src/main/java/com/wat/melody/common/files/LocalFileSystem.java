package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.HiddenException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.SymbolicLinkNotSupported;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.files.exception.WrapperNotDirectoryException;
import com.wat.melody.common.files.exception.WrapperNotLinkException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalFileSystem implements FileSystem {

	public static Path convertToLocalPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return Paths.get(path);
	}

	public static Path convertToLocalPath(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return convertToLocalPath(path.toString());
	}

	public LocalFileSystem() {
	}

	@Override
	public void release() {
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.exists(path, options);
	}

	@Override
	public boolean exists(String path, LinkOption... options) {
		return exists(convertToLocalPath(path), options);
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.isDirectory(path, options);
	}

	@Override
	public boolean isDirectory(String path, LinkOption... options) {
		return isDirectory(convertToLocalPath(path), options);
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.isRegularFile(path, options);
	}

	@Override
	public boolean isRegularFile(String path, LinkOption... options) {
		return isRegularFile(convertToLocalPath(path), options);
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.isSymbolicLink(path);
	}

	@Override
	public boolean isSymbolicLink(String path) {
		return isSymbolicLink(convertToLocalPath(path));
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (dir == null || dir.toString().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			Files.createDirectory(dir);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (FileAlreadyExistsException Ex) {
			// the dir already exists => no error
			// a link or file exists => error
			if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				throw new WrapperFileAlreadyExistsException(Ex.getFile(), Ex);
			}
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		} catch (FileSystemException Ex) {
			if (Ex.getMessage() != null
					&& Ex.getMessage().indexOf(": Not a directory") != -1) {
				throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
			} else {
				throw Ex;
			}
		}
		setAttributes(dir, attrs);
	}

	@Override
	public void createDirectory(String dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createDirectory(convertToLocalPath(dir), attrs);
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			IllegalFileAttributeException, AccessDeniedException {
		if (dir == null || dir.toString().length() == 0
				|| dir.getNameCount() < 1) {
			return;
		}
		try {
			createDirectory(dir, attrs);
			return;
		} catch (NoSuchFileException Ex) {
			// if the top first dir cannot be created => raise an error
			if (dir.getNameCount() <= 1) {
				throw Ex;
			}
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir => no error
			try {
				if (readAttributes0(dir).isDirectory()) {
					return;
				}
			} catch (NoSuchFileException Exx) {
				// concurrency pb : should recreate ?
			}
			throw Ex;
		} catch (IOException Ex) {
			throw Ex;
		}
		// if dir cannot be created => create its parent
		Path parent = null;
		parent = dir.resolve("..").normalize();
		createDirectories(parent, attrs);
		try {
			createDirectory(dir, attrs);
		} catch (FileAlreadyExistsException Ex) {
			// if the file is a link on a dir => no error
			try {
				if (readAttributes0(dir).isDirectory()) {
					return;
				}
			} catch (NoSuchFileException Exx) {
				// concurrency pb : should recreate ?
			}
			throw Ex;
		}
	}

	@Override
	public void createDirectories(String dir, FileAttribute<?>... attrs)
			throws IOException, IllegalFileAttributeException,
			FileAlreadyExistsException, AccessDeniedException {
		createDirectories(convertToLocalPath(dir));
	}

	@Override
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (link == null || link.toString().length() == 0) {
			throw new IllegalArgumentException(link + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		if (target == null || target.toString().length() == 0) {
			throw new IllegalArgumentException(target + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			Files.createSymbolicLink(link, target);
		} catch (UnsupportedOperationException Ex) {
			throw new SymbolicLinkNotSupported(link, target, Ex);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (FileAlreadyExistsException Ex) {
			throw new WrapperFileAlreadyExistsException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
		setAttributes(link, attrs);
	}

	@Override
	public void createSymbolicLink(String link, String target,
			FileAttribute<?>... attrs) throws IOException,
			SymbolicLinkNotSupported, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		createSymbolicLink(convertToLocalPath(link), convertToLocalPath(target));
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		if (link == null || link.toString().length() == 0) {
			throw new IllegalArgumentException(link + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			// /!\ This will remove the trailing '/'
			return Paths.get(Files.readSymbolicLink(link).toString());
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (NotLinkException Ex) {
			throw new WrapperNotLinkException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	@Override
	public Path readSymbolicLink(String link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		return readSymbolicLink(convertToLocalPath(link));
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			Files.delete(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	@Override
	public void delete(String path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		delete(convertToLocalPath(path));
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			return Files.deleteIfExists(path);
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	@Override
	public boolean deleteIfExists(String path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		return deleteIfExists(convertToLocalPath(path));
	}

	@Override
	public void deleteDirectory(Path dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		try {
			for (Path entry : newDirectoryStream(dir)) {
				if (isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
					deleteDirectory(entry);
				} else {
					deleteIfExists(entry);
				}
			}
		} catch (NoSuchFileException ignored) {
		}
		deleteIfExists(dir);
	}

	@Override
	public void deleteDirectory(String dir) throws IOException,
			NotDirectoryException, AccessDeniedException {
		deleteDirectory(convertToLocalPath(dir));
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NotDirectoryException, NoSuchFileException,
			AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			return Files.newDirectoryStream(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (NotDirectoryException Ex) {
			throw new WrapperNotDirectoryException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(String path)
			throws IOException, NotDirectoryException, NoSuchFileException,
			AccessDeniedException {
		return newDirectoryStream(convertToLocalPath(path));
	}

	@Override
	public LocalFileAttributes readAttributes(Path path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		BasicFileAttributes pathAttrs = readAttributes0(path,
				LinkOption.NOFOLLOW_LINKS);
		Path target = null;
		BasicFileAttributes realAttrs = null;
		if (pathAttrs.isSymbolicLink()) {
			target = readSymbolicLink(path);
			try {
				realAttrs = readAttributes0(path);
			} catch (NoSuchFileException ignored) {
			}
		}
		return new LocalFileAttributes(pathAttrs, target, realAttrs);
	}

	@Override
	public LocalFileAttributes readAttributes(String path) throws IOException,
			NoSuchFileException, AccessDeniedException {
		return readAttributes(convertToLocalPath(path));
	}

	private BasicFileAttributes readAttributes0(Path path,
			LinkOption... options) throws IOException, NoSuchFileException,
			AccessDeniedException {
		try {
			return Files.readAttributes(path, BasicFileAttributes.class,
					options);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	@Override
	public void setAttributes(Path path, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		if (attrs == null || attrs.length == 0) {
			return;
		}
		ConsolidatedException full = new ConsolidatedException(Msg.bind(
				Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTES, path));
		for (FileAttribute<?> attr : attrs) {
			if (attr == null) {
				continue;
			}
			try {
				/*
				 * will throw NullPointerException if attr.name() is null.
				 * 
				 * will throw UnsupportedOperationException if attr.name()
				 * denotes an unrecognized view.
				 * 
				 * will throw IllegalArgumentException if attr.name() denotes a
				 * know view but an unrecognized attribute.
				 * 
				 * will throw NullPointerException if attr.value() is null.
				 * 
				 * will throw ClassCastException if attr.name() denotes a known
				 * view and a known but attr.value() is not of the correct type.
				 * 
				 * will throw IllegalArgumentException if attr.name() denotes a
				 * known view and a known and attr.value() is of the correct
				 * type but as an invalid value.
				 * 
				 * will throw IOException the operation failed (access denied,
				 * no such file, permissions on symbolic link...).
				 */
				setAttribute(path, attr.name(), attr.value(),
						LinkOption.NOFOLLOW_LINKS);
			} catch (WrapperNoSuchFileException | WrapperAccessDeniedException Ex) {
				// only need the reason
				full.addCause(new MelodyException(Msg.bind(
						Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr,
						Ex.getReason()), new HiddenException(Ex)));
			} catch (FileSystemException Ex) {
				// don't want neither the stack trace nor the file name
				String msg = Ex.getReason();
				if (msg == null || msg.length() == 0) {
					msg = Ex.getClass().getName();
				} else {
					msg = Ex.getClass().getName() + " - " + msg;
				}
				full.addCause(new MelodyException(Msg.bind(
						Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr, msg),
						new HiddenException(Ex)));
			} catch (IOException Ex) {
				full.addCause(new MelodyException(Msg.bind(
						Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			} catch (UnsupportedOperationException | IllegalArgumentException
					| ClassCastException Ex) {
				// don't want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex),
						new HiddenException(Ex)));
			} catch (Throwable Ex) {
				// want the stack trace
				full.addCause(new MelodyException(Msg.bind(
						Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex));
			}
		}
		if (full.countCauses() != 0) {
			throw new IllegalFileAttributeException(full);
		}
	}

	@Override
	public void setAttributes(String path, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			IllegalFileAttributeException, AccessDeniedException {
		setAttributes(convertToLocalPath(path), attrs);
	}

	private void setAttribute(Path path, String attrName, Object attrValue,
			LinkOption... linkOptions) throws IOException, NoSuchFileException,
			AccessDeniedException {
		try {
			Files.setAttribute(path, attrName, attrValue, linkOptions);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

}