package com.wat.melody.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.HiddenException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.files.exception.IllegalTarGzException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FS {

	private static Logger log = LoggerFactory.getLogger(FS.class);

	/**
	 * <p>
	 * Raise an error if the given path is not an existing readable/writable
	 * directory.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the directory to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non existing
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             directory ;</li>
	 *             </ul>
	 */
	public static void validateDirExists(String path)
			throws IllegalDirectoryException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Directory Path).");
		}

		File item = new File(path);
		if (item.isFile()) {
			throw new IllegalDirectoryException(Msg.bind(
					Messages.DirEx_NOT_A_DIR, path));
		} else if (item.isDirectory()) {
			if (!item.canRead()) {
				throw new IllegalDirectoryException(Msg.bind(
						Messages.DirEx_CANT_READ, path));
			} else if (!item.canWrite()) {
				throw new IllegalDirectoryException(Msg.bind(
						Messages.DirEx_CANT_WRITE, path));
			}
		} else if (!item.exists()) {
			if (item.getParent() != null) {
				try {
					validateDirExists(item.getParent());
				} catch (IllegalDirectoryException Ex) {
					throw new IllegalDirectoryException(Msg.bind(
							Messages.DirEx_NOT_FOUND, path), Ex);
				}
			}
			throw new IllegalDirectoryException(Msg.bind(
					Messages.DirEx_NOT_FOUND, path));
		}
	}

	/**
	 * <p>
	 * Raise an error if the given path is not a valid readable/writable
	 * directory.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the directory to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDirectoryException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             directory ;</li>
	 *             </ul>
	 */
	public static void validateDirPath(String path)
			throws IllegalDirectoryException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Directory Path).");
		}

		File item = new File(path);
		if (item.isFile()) {
			throw new IllegalDirectoryException(Msg.bind(
					Messages.DirEx_NOT_A_DIR, path));
		} else if (item.isDirectory()) {
			if (!item.canRead()) {
				throw new IllegalDirectoryException(Msg.bind(
						Messages.DirEx_CANT_READ, path));
			} else if (!item.canWrite()) {
				throw new IllegalDirectoryException(Msg.bind(
						Messages.DirEx_CANT_WRITE, path));
			}
		} else if (item.getParent() != null) {
			try {
				validateDirPath(item.getParent());
			} catch (IllegalDirectoryException Ex) {
				throw new IllegalDirectoryException(Msg.bind(
						Messages.DirEx_INVALID_PARENT, path), Ex);
			}
		}
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not an existing readable/writable
	 * file.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the file to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non existing
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             file ;</li>
	 *             </ul>
	 */
	public static void validateFileExists(String path)
			throws IllegalFileException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}

		File item = new File(path);
		if (item.isDirectory()) {
			throw new IllegalFileException(Msg.bind(Messages.FileEx_NOT_A_FILE,
					path));
		} else if (item.isFile()) {
			if (!item.canRead()) {
				throw new IllegalFileException(Msg.bind(
						Messages.FileEx_CANT_READ, path));
			} else if (!item.canWrite()) {
				throw new IllegalFileException(Msg.bind(
						Messages.FileEx_CANT_WRITE, path));
			}
		} else if (!item.exists()) {
			if (item.getParent() != null) {
				try {
					validateDirExists(item.getParent());
				} catch (IllegalDirectoryException Ex) {
					throw new IllegalFileException(Msg.bind(
							Messages.FileEx_NOT_FOUND, path), Ex);
				}
			}
			throw new IllegalFileException(Msg.bind(Messages.FileEx_NOT_FOUND,
					path));
		}
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not a valid readable/writable
	 * file.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the file to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             file ;</li>
	 *             </ul>
	 * @throws IllegalDirectoryException
	 *             <ul>
	 *             <li>if the given file's parent points to non readable
	 *             directory ;</li>
	 *             <li>if the given file's parent points to non writable
	 *             directory ;</li>
	 *             </ul>
	 */
	public static void validateFilePath(String path)
			throws IllegalFileException, IllegalDirectoryException {
		if (path == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}

		File item = new File(path);
		if (item.isDirectory()) {
			throw new IllegalFileException(Msg.bind(Messages.FileEx_NOT_A_FILE,
					item));
		}
		if (item.exists() && !item.canRead()) {
			throw new IllegalFileException(Msg.bind(Messages.FileEx_CANT_READ,
					item));
		}
		if (item.exists() && !item.canWrite()) {
			throw new IllegalFileException(Msg.bind(Messages.FileEx_CANT_WRITE,
					item));
		}
		validateDirPath(item.getParent());
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not a valid readable/writable
	 * TarGz archive.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the TarGz to validate.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTarGzException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a directory ;</li>
	 *             <li>if the given <tt>String</tt> points to non existing file
	 *             ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to non valid TarGz
	 *             archive ;</li>
	 *             </ul>
	 */
	public static void validateTarGzExists(String path)
			throws IllegalTarGzException {
		try {
			validateFileExists(path);
		} catch (IllegalFileException Ex) {
			throw new IllegalTarGzException(Msg.bind(
					Messages.TarGzEx_NOT_A_TARGZ, path), Ex);
		}
		if (!path.endsWith("tar.gz")) {
			throw new IllegalTarGzException(Msg.bind(
					Messages.TarGzEx_INVALID_EXTENSION, path));
		}
		// TODO : perform a real extraction test
	}

	/**
	 * @param path
	 *            is the path of the TarGz archive to validate.
	 * 
	 * @return <tt>true</tt> if the given path points to a valid TarGz archive,
	 *         or <tt>false</tt> otherwise (e.g. a directory, a non readable
	 *         file, a non writable file, a non existent file, a file which is
	 *         not a valid TarGz archive).
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static boolean isTarGz(String path) {
		try {
			validateTarGzExists(path);
		} catch (IllegalTarGzException Ex) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Extract the given TarGz archive into the given destination directory.
	 * </p>
	 * 
	 * <ul>
	 * <li>If the given destination directory doesn't exists, it will be
	 * created, including any necessary but nonexistent parent directories ;</li>
	 * <li>Note that if this operation fails it may have succeeded in creating
	 * some of the necessary parent directories ;</li>
	 * </ul>
	 * 
	 * @param path
	 *            is the path of the TarGz archive.
	 * @param sOutPutDir
	 *            is the path of the directory where the archive will be
	 *            extracted.
	 * @param continueOnAtributeError
	 *            indicate if this method should fail (or not) if an error
	 *            occurred when setting an entry's user-id, owner-id or
	 *            permissions
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given TarGz archive is <tt>null</tt> ;</li>
	 *             <li>if the given destination directory is <tt>null</tt>< ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred.
	 * @throws IllegalFileAttributeException
	 *             if <tt>continueOnAtributeError</tt> is <tt>false</tt> and an
	 *             error occurred when setting an entry's user-id, owner-id or
	 *             permissions.
	 * @throws IllegalTarGzException
	 *             <ul>
	 *             <li>if the given TarGz archive points to a directory ;</li>
	 *             <li>if the given TarGz archive points to non existing file ;</li>
	 *             <li>if the given TarGz archive points to a non readable file
	 *             ;</li>
	 *             <li>if the given TarGz archive points to a non writable file
	 *             ;</li>
	 *             <li>if the given TarGz archive points to non valid TarGz
	 *             archive ;</li>
	 *             </ul>
	 * @throws IllegalDirectoryException
	 *             <ul>
	 *             <li>if the given destination directory points to a file ;</li>
	 *             <li>if the given destination directory points to a non
	 *             readable directory ;</li>
	 *             <li>if the given destination directory points to a non
	 *             writable directory ;</li>
	 *             </ul>
	 */
	public static void extractTarGz(String path, String outdir,
			boolean continueOnAtributeError) throws IOException,
			IllegalFileAttributeException, IllegalTarGzException,
			IllegalDirectoryException {
		// Validate input parameter
		// If the given path is not a valid targz archive => raise an error
		validateTarGzExists(path);
		// If the given output directory is not a valid directory => raise an
		// error
		validateDirPath(outdir);
		// Create the directory (including any necessary but nonexistent parent
		// directories) if it doesn't exists
		new File(outdir).mkdirs();

		FileInputStream fis = null;
		GZIPInputStream gzis = null;
		TarArchiveInputStream tis = null;
		FileOutputStream fos = null;
		try {
			// Create the file input stream from the file
			fis = new FileInputStream(new File(path));
			// Unzip it (no need to use a BufferedInputStream because
			// GZIPInputStream has its own buffer)
			gzis = new GZIPInputStream(fis);
			// Untar it
			tis = new TarArchiveInputStream(gzis);
			TarArchiveEntry entry = null;
			// For each entry in the tar archive
			while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
				// Create a new File instance in the given output directory
				Path outpath = Paths.get(outdir, entry.getName());
				if (entry.isSymbolicLink()) {
					if (!Files.exists(outpath)) {
						Files.createSymbolicLink(outpath,
								Paths.get(entry.getLinkName()));
					}
					// what to do if the file exists ? Should callback
					applyAttributes(outpath, entry, continueOnAtributeError);
				} else if (entry.isFile()) {
					if (!Files.exists(outpath)) {
						// If the current entry is a file => extract it
						fos = new FileOutputStream(outpath.toFile());
						IOUtils.copy(tis, fos);
						fos.flush();
						fos.close();
						fos = null;
					}
					// what to do if the file exists ? Should callback
					applyAttributes(outpath, entry, continueOnAtributeError);
				} else if (entry.isDirectory()) {
					if (!Files.exists(outpath)) {
						// If the current entry is a directory => create it
						Files.createDirectory(outpath);
					}
					// what to do if the file exists ? Should callback
					applyAttributes(outpath, entry, continueOnAtributeError);
				} else if (entry.isFIFO()) {
					log.info(outpath + ": pipes are not supported.");
				} else if (entry.isLink()) {
					log.info(outpath + ": links are not supported.");
				} else if (entry.isBlockDevice()) {
					log.info(outpath + ": block devices are not supported.");
				} else if (entry.isCharacterDevice()) {
					log.info(outpath + ": character devices are not supported.");
				}
			}
		} finally {
			// Close all streams
			if (fos != null)
				fos.close();
			if (tis != null)
				tis.close();
			if (gzis != null)
				gzis.close();
			if (fis != null)
				fis.close();
		}
	}

	private static void applyAttributes(Path path, TarArchiveEntry entry,
			boolean continueOnAtributeError)
			throws IllegalFileAttributeException {
		ConsolidatedException full = new ConsolidatedException(Msg.bind(
				Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTES, path));

		try {
			PosixUser user = new PosixUser(entry.getUserId());
			applyAttribute(path, "owner:owner", user.toUserPrincipal());
		} catch (MelodyException Ex) {
			full.addCause(Ex);
		}

		try {
			PosixGroup group = new PosixGroup(entry.getGroupId());
			applyAttribute(path, "posix:group", group.toGroupPrincipal());
		} catch (MelodyException Ex) {
			full.addCause(Ex);
		}

		try {
			PosixPermissions perms = new PosixPermissions(entry.getMode());
			applyAttribute(path, "posix:permissions",
					perms.toPosixFilePermissionSet());
		} catch (MelodyException Ex) {
			full.addCause(Ex);
		}

		if (full.countCauses() != 0) {
			if (continueOnAtributeError) {
				log.info(new MelodyException(full).getUserFriendlyStackTrace());
			} else {
				throw new IllegalFileAttributeException(full);
			}
		}
	}

	private static void applyAttribute(Path path, String name, Object value)
			throws MelodyException {
		String attr = name + "=" + value;
		try {
			setAttribute(path, name, value, LinkOption.NOFOLLOW_LINKS);
		} catch (WrapperNoSuchFileException | WrapperAccessDeniedException Ex) {
			// only need the reason
			throw new MelodyException(Msg.bind(
					Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr,
					Ex.getReason()), new HiddenException(Ex));
		} catch (FileSystemException Ex) {
			// don't want neither the stack trace nor the file name
			String msg = Ex.getReason();
			if (msg == null || msg.length() == 0) {
				msg = Ex.getClass().getName();
			} else {
				msg = Ex.getClass().getName() + " - " + msg;
			}
			throw new MelodyException(Msg.bind(
					Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr, msg),
					new HiddenException(Ex));
		} catch (IOException Ex) {
			throw new MelodyException(Msg.bind(
					Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex);
		} catch (UnsupportedOperationException | IllegalArgumentException
				| ClassCastException Ex) {
			// don't want the stack trace
			throw new MelodyException(Msg.bind(
					Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE, attr, Ex),
					new HiddenException(Ex));
		} catch (Throwable Ex) {
			// want the stack trace
			throw new MelodyException(Msg.bind(
					Messages.LocalFSEx_FAILED_TO_SET_ATTRIBUTE_X, attr), Ex);
		}
	}

	private static void setAttribute(Path path, String attrName,
			Object attrValue, LinkOption... linkOptions) throws IOException,
			NoSuchFileException, AccessDeniedException {
		try {
			Files.setAttribute(path, attrName, attrValue, linkOptions);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile(), Ex);
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile(), Ex);
		}
	}

	/**
	 * <p>
	 * Delete the given directory and all its content. After deletion, delete
	 * the parent directory if it is empty and so on.
	 * </p>
	 * 
	 * @param path
	 *            is the path of the directory to delete.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDirectoryException
	 *             <ul>
	 *             <li>if sPath points to a file ;</li>
	 *             <li>if sPath points to a non readable directory ;</li>
	 *             <li>if sPath points to a non writable directory ;</li>
	 *             </ul>
	 * @throws AccessDeniedException
	 *             if the given directory, its content or an empty parent
	 *             directory cannot be deleted because of permissions issue.
	 * @throws IOException
	 *             if an IO error occurred while deleting a directory.
	 */
	public static void deleteDirectoryAndEmptyParentDirectory(String path)
			throws IOException, IllegalDirectoryException,
			AccessDeniedException {
		validateDirPath(path);
		FileSystem fs = new LocalFileSystem();
		Path dir = Paths.get(path).normalize();
		fs.deleteDirectory(dir);
		Path parentDir = dir;
		while ((parentDir = parentDir.getParent()) != null) {
			try {
				if (fs.newDirectoryStream(parentDir).iterator().hasNext()) {
					break;
				}
			} catch (NoSuchFileException ignored) {
				break;
			}
			fs.deleteDirectory(parentDir);
		}
	}

	public static int validateBashSyntax(String bashToValidate,
			StringBuilder outstr) throws IOException, InterruptedException {
		// pour le debuggage sous windows
		// if ( sBashToValidate.compareTo("toto") != 0 ) return 0;
		InputStream is = null;
		try {
			String[] oCmdLine = { "/bin/sh", "-n", bashToValidate };
			Process oP = Runtime.getRuntime().exec(oCmdLine);
			outstr.append("\n\t");
			is = oP.getErrorStream();
			int c;
			while ((c = is.read()) != -1)
				if (c == 10)
					outstr.append((char) c + "\t");
				else
					outstr.append((char) c);
			is.close();
			int nRes = oP.waitFor();
			oP.destroy();
			SysTool.replaceAll(outstr, bashToValidate + ":", "");
			return nRes;
		} finally {
			if (is != null)
				is.close();
		}
	}

}