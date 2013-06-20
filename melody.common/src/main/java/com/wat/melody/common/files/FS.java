package com.wat.melody.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.files.exception.IllegalTarGzException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FS {

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
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given TarGz archive is <tt>null</tt> ;</li>
	 *             <li>if the given destination directory is <tt>null</tt>< ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred.
	 * @throws IllegalTarGzException
	 *             if sPath points to a directory.
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
	public static void extractTarGz(String path, String outdir)
			throws IOException, IllegalTarGzException,
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
				final File outputFile = new File(outdir, entry.getName());
				if (entry.isFile()) {
					// If the current entry is a file => extract it to the disk
					// (using a file output stream)
					// Note the if the file already exists, it will be replaced
					fos = new FileOutputStream(outputFile);
					IOUtils.copy(tis, fos);
					fos.flush();
					fos.close();
					fos = null;
					// TODO : Set file attributes (user id, group id, ...)
				} else if (outputFile.exists() || outputFile.mkdirs()) {
					// If the current entry is a directory => create it
					// TODO : Set directory attributes (user id, group id, ...)
					continue;
				} else {
					// If an error occurred while creating the directory =>
					// raise an
					// error
					throw new IOException("Couldn't create directory '"
							+ outputFile.getAbsolutePath() + "'.");
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
	 * @throws IOException
	 *             if an IO error occurred while deleting a directory.
	 * @throws IllegalDirectoryException
	 *             <ul>
	 *             <li>if sPath points to a file ;</li>
	 *             <li>if sPath points to a non readable directory ;</li>
	 *             <li>if sPath points to a non writable directory ;</li>
	 *             </ul>
	 */
	public static void deleteDirectoryAndEmptyParentDirectory(String path)
			throws IOException, IllegalDirectoryException {
		validateDirPath(path);
		File dir = new File(path);
		FileUtils.deleteDirectory(dir);
		File parentDir = dir;
		while (true) {
			parentDir = parentDir.getParentFile();
			String[] content = parentDir.list();
			if (content == null) {
				break;
			} else if (content.length != 0) {
				break;
			}
			FileUtils.deleteDirectory(parentDir);
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