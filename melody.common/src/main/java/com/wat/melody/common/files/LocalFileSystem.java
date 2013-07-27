package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static Logger log = LoggerFactory.getLogger(LocalFileSystem.class);

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
	public boolean isDirectory(Path path, LinkOption... options) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.isDirectory(path, options);
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
	public boolean isSymbolicLink(Path path) {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		return Files.isSymbolicLink(path);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		if (dir == null || dir.toString().length() == 0) {
			throw new IllegalArgumentException(dir + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			Files.createDirectory(dir);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (FileAlreadyExistsException Ex) {
			if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				throw new WrapperFileAlreadyExistsException(Ex.getFile());
			}
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
		setAttributes(dir, attrs);
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException {
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
			// if the file is a link on a dir or a dir => no error
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
			// if the file is a link on a dir or a dir => no error
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
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
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
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (FileAlreadyExistsException Ex) {
			throw new WrapperFileAlreadyExistsException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
		setAttributes(link, attrs);
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
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (NotLinkException Ex) {
			throw new WrapperNotLinkException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
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
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
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
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
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
	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NotDirectoryException, NoSuchFileException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		try {
			return Files.newDirectoryStream(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (NotDirectoryException Ex) {
			throw new WrapperNotDirectoryException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			AccessDeniedException {
		try {
			return _readAttributes(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	private EnhancedFileAttributes _readAttributes(Path path)
			throws IOException, NoSuchFileException, AccessDeniedException {
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

	private BasicFileAttributes readAttributes0(Path path,
			LinkOption... options) throws IOException, NoSuchFileException,
			AccessDeniedException {
		return Files.readAttributes(path, BasicFileAttributes.class, options);
	}

	@Override
	public void setAttributes(Path path, FileAttribute<?>... attributes)
			throws IOException, NoSuchFileException, AccessDeniedException {
		if (path == null || path.toString().length() == 0) {
			throw new IllegalArgumentException(path + ": Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		if (attributes == null) {
			return;
		}
		/*
		 * TODO: should be wrapped in an IllegalFileAttributeException, with
		 * responsibilities to the caller to deal with.
		 */
		for (FileAttribute<?> attr : attributes) {
			if (attr == null) {
				continue;
			}
			try {
				Files.setAttribute(path, attr.name(), attr.value(),
						LinkOption.NOFOLLOW_LINKS);
			} catch (UnsupportedOperationException | IllegalArgumentException
					| ClassCastException Ex) {
				log.warn(Msg.bind(Messages.LocalFSMsg_SKIP_ATTR, attr, path));
			}

		}
	}

}