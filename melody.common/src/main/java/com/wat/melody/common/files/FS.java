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

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FS {

	/**
	 * Is equal to System.getProperty("file.separator")
	 */
	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	/**
	 * Is equal to System.getProperty("java.io.tmpdir")
	 */
	public static final String SYSTEM_TEMP_DIR = System
			.getProperty("java.io.tmpdir");

	/**
	 * <p>
	 * Raise an error if the given path is not an existing readable/writable
	 * directory.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the directory to validate.
	 * 
	 * @throws IllegalDirectoryException
	 *             if sPath points to a file.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non readable directory.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non writable directory.
	 * @throws IllegalDirectoryException
	 *             if sPath points to non existing directory.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 * 
	 */
	public static void validateDirExists(String sPath)
			throws IllegalDirectoryException {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Directory Path).");
		}

		/*
		 * TODO : use the java.nio instead
		 */
		File item = new File(sPath);
		if (item.isFile()) {
			throw new IllegalDirectoryException(Messages.bind(
					Messages.DirEx_NOT_A_DIR, sPath));
		} else if (item.isDirectory()) {
			if (!item.canRead()) {
				throw new IllegalDirectoryException(Messages.bind(
						Messages.DirEx_CANT_READ, sPath));
			} else if (!item.canWrite()) {
				throw new IllegalDirectoryException(Messages.bind(
						Messages.DirEx_CANT_WRITE, sPath));
			}
		} else if (!item.exists()) {
			if (item.getParent() != null) {
				try {
					validateDirExists(item.getParent());
				} catch (IllegalDirectoryException Ex) {
					throw new IllegalDirectoryException(Messages.bind(
							Messages.DirEx_NOT_FOUND, sPath), Ex);
				}
			}
			throw new IllegalDirectoryException(Messages.bind(
					Messages.DirEx_NOT_FOUND, sPath));
		}
	}

	/**
	 * <p>
	 * Raise an error if the given path is not a valid readable/writable
	 * directory.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the directory to validate.
	 * 
	 * @throws IllegalDirectoryException
	 *             if sPath points to a file.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non readable directory.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non writable directory.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 * 
	 */
	public static void validateDirPath(String sPath)
			throws IllegalDirectoryException {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Directory Path).");
		}

		File item = new File(sPath);
		if (item.isFile()) {
			throw new IllegalDirectoryException(Messages.bind(
					Messages.DirEx_NOT_A_DIR, sPath));
		} else if (item.isDirectory()) {
			if (!item.canRead()) {
				throw new IllegalDirectoryException(Messages.bind(
						Messages.DirEx_CANT_READ, sPath));
			} else if (!item.canWrite()) {
				throw new IllegalDirectoryException(Messages.bind(
						Messages.DirEx_CANT_WRITE, sPath));
			}
		} else if (item.getParent() != null) {
			try {
				validateDirPath(item.getParent());
			} catch (IllegalDirectoryException Ex) {
				throw new IllegalDirectoryException(Messages.bind(
						Messages.DirEx_INVALID_PARENT, sPath), Ex);
			}
		}
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not an existing readable/writable
	 * file.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to validate.
	 * 
	 * @throws IllegalFileException
	 *             if sPath points to a directory.
	 * @throws IllegalFileException
	 *             if sPath points to a non readable file.
	 * @throws IllegalFileException
	 *             if sPath points to a non writable file.
	 * @throws IllegalFileException
	 *             if sPath points to non existing file.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 */
	public static void validateFileExists(String sPath)
			throws IllegalFileException {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}

		File item = new File(sPath);
		if (item.isDirectory()) {
			throw new IllegalFileException(Messages.bind(
					Messages.FileEx_NOT_A_FILE, sPath));
		} else if (item.isFile()) {
			if (!item.canRead()) {
				throw new IllegalFileException(Messages.bind(
						Messages.FileEx_CANT_READ, sPath));
			} else if (!item.canWrite()) {
				throw new IllegalFileException(Messages.bind(
						Messages.FileEx_CANT_WRITE, sPath));
			}
		} else if (!item.exists()) {
			if (item.getParent() != null) {
				try {
					validateDirExists(item.getParent());
				} catch (IllegalDirectoryException Ex) {
					throw new IllegalFileException(Messages.bind(
							Messages.FileEx_NOT_FOUND, sPath), Ex);
				}
			}
			throw new IllegalFileException(Messages.bind(
					Messages.FileEx_NOT_FOUND, sPath));
		}
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not a valid readable/writable
	 * file.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the file to validate.
	 * 
	 * @throws IllegalFileException
	 *             if sPath points to a directory.
	 * @throws IllegalFileException
	 *             if sPath points to a non readable file.
	 * @throws IllegalFileException
	 *             if sPath points to a non writable file.
	 * @throws IllegalDirectoryException
	 *             if sPath's parent points to non readable directory.
	 * @throws IllegalDirectoryException
	 *             if sPath's parent points to non writable directory.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 */
	public static void validateFilePath(String sPath)
			throws IllegalFileException, IllegalDirectoryException {
		if (sPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}

		File item = new File(sPath);
		if (item.isDirectory()) {
			throw new IllegalFileException(Messages.bind(
					Messages.FileEx_NOT_A_FILE, item));
		}
		if (item.exists() && !item.canRead()) {
			throw new IllegalFileException(Messages.bind(
					Messages.FileEx_CANT_READ, item));
		}
		if (item.exists() && !item.canWrite()) {
			throw new IllegalFileException(Messages.bind(
					Messages.FileEx_CANT_WRITE, item));
		}
		validateDirPath(item.getParent());
	}

	/**
	 * <p>
	 * Raise an exception if the given path is not a valid readable/writable
	 * TarGz archive.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the TarGz to validate.
	 * 
	 * @throws IllegalTarGzException
	 *             if sPath points to a directory.
	 * @throws IllegalTarGzException
	 *             if sPath points to a non readable file.
	 * @throws IllegalTarGzException
	 *             if sPath points to a non writable file.
	 * @throws IllegalTarGzException
	 *             if sPath points to non existing file.
	 * @throws IllegalTarGzException
	 *             if sPath points to non valid TarGz archive.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 */
	public static void validateTarGzExists(String sPath)
			throws IllegalTarGzException {
		try {
			validateFileExists(sPath);
		} catch (IllegalFileException Ex) {
			throw new IllegalTarGzException(Messages.bind(
					Messages.TarGzEx_NOT_A_TARGZ, sPath), Ex);
		}
		if (!sPath.endsWith("tar.gz")) {
			throw new IllegalTarGzException(Messages.bind(
					Messages.TarGzEx_INVALID_EXTENSION, sPath));
		}
		// TODO : perform a real extraction test
	}

	/**
	 * <p>
	 * Return <code>true</code> if the given path points to a valid TarGz
	 * archive.
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the TarGz archive to validate.
	 * 
	 * @return if sPath points to a directory, or if sPath points to a non
	 *         readable file, or if sPath points to a non writable file, or if
	 *         sPath points to a non existent file, or if sPath points to a file
	 *         which is not a valid TarGz archive.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <code>null</code>.
	 * 
	 */
	public static boolean isTarGz(String sPath) {
		try {
			validateTarGzExists(sPath);
		} catch (IllegalTarGzException Ex) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Extract the given TarGz archive into the given directory.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the given directory doesn't exists, it will be created,
	 * including any necessary but nonexistent parent directories. <BR/>
	 * * Note that if this operation fails it may have succeeded in creating
	 * some of the necessary parent directories. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sPath
	 *            is the path of the TarGz archive.
	 * @param sOutPutDir
	 *            is the path of the directory where the archive will be
	 *            extracted.
	 * 
	 * @throws IllegalTarGzException
	 *             if sPath points to a directory.
	 * @throws IllegalTarGzException
	 *             if sPath points to a non readable file.
	 * @throws IllegalTarGzException
	 *             if sPath points to a non writable file.
	 * @throws IllegalTarGzException
	 *             if sPath points to a non existent file.
	 * @throws IllegalTarGzException
	 *             if sPath points to an invalid TarGz archive.
	 * @throws IllegalDirectoryException
	 *             if sOutPutDir points to a file.
	 * @throws IllegalDirectoryException
	 *             if sOutPutDir points to a non readable directory.
	 * @throws IllegalDirectoryException
	 *             if sOutPutDir points to a non writable directory.
	 * @throws IOException
	 *             if an IO error occurred while reading the TarGz archive.
	 * @throws IOException
	 *             if an IO error occurred while reading an entry of the TarGz
	 *             archive.
	 * @throws IOException
	 *             if an IO error occurred while creating a file entry.
	 * @throws IOException
	 *             if an IO error occurred while creating a directory entry.
	 * @throws IllegalArgumentException
	 *             if sPath is null.
	 * @throws IllegalArgumentException
	 *             if sOutPutDir is null.
	 * 
	 */
	public static void extractTarGz(String sPath, String sOutputDir)
			throws IOException, IllegalTarGzException,
			IllegalDirectoryException {
		// Validate input parameter
		// If the given path is not a valid targz archive => raise an error
		validateTarGzExists(sPath);
		// If the given output directory is not a valid directory => raise an
		// error
		validateDirPath(sOutputDir);
		// Create the directory (including any necessary but nonexistent parent
		// directories) if it doesn't exists
		new File(sOutputDir).mkdirs();

		FileInputStream fis = null;
		GZIPInputStream gzis = null;
		TarArchiveInputStream tis = null;
		FileOutputStream fos = null;
		try {
			// Create the file input stream from the file
			fis = new FileInputStream(new File(sPath));
			// Unzip it (no need to use a BufferedInputStream because
			// GZIPInputStream has its own buffer)
			gzis = new GZIPInputStream(fis);
			// Untar it
			tis = new TarArchiveInputStream(gzis);
			TarArchiveEntry entry = null;
			// For each entry in the tar archive
			while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
				// Create a new File instance in the given output directory
				final File outputFile = new File(sOutputDir, entry.getName());
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
	 * @param sPath
	 *            is the path of the directory to delete.
	 * 
	 * @throws IllegalDirectoryException
	 *             if sPath points to a file.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non readable directory.
	 * @throws IllegalDirectoryException
	 *             if sPath points to a non writable directory.
	 * @throws IOException
	 *             if an IO error occurred while deleting a directory.
	 * @throws IllegalArgumentException
	 *             if sPath is <code>null</code>.
	 * 
	 */
	public static void deleteDirectoryAndEmptyParentDirectory(String sPath)
			throws IOException, IllegalDirectoryException {
		validateDirPath(sPath);
		File dir = new File(sPath);
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

	public static void replaceAll(StringBuilder builder, String from, String to) {
		int index = builder.indexOf(from);
		while (index != -1) {
			builder.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = builder.indexOf(from, index);
		}
	}

	public static int validateBashSyntax(String sBashToValidate,
			StringBuilder sOutRes) throws IOException, InterruptedException {
		// pour le debuggage sous windows
		// if ( sBashToValidate.compareTo("toto") != 0 ) return 0;
		InputStream is = null;
		try {
			String[] oCmdLine = { "/bin/sh", "-n", sBashToValidate };
			Process oP = Runtime.getRuntime().exec(oCmdLine);
			sOutRes.append("\n\t");
			is = oP.getErrorStream();
			int c;
			while ((c = is.read()) != -1)
				if (c == 10)
					sOutRes.append((char) c + "\t");
				else
					sOutRes.append((char) c);
			is.close();
			int nRes = oP.waitFor();
			oP.destroy();
			FS.replaceAll(sOutRes, sBashToValidate + ":", "");
			return nRes;
		} finally {
			if (is != null)
				is.close();
		}
	}

}