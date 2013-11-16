package com.wat.melody.common.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperDirectoryAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperSymbolicLinkAlreadyExistsException;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferHelper {

	private static Logger log = LoggerFactory.getLogger(TransferHelper.class);

	protected static void createDirectory(FileSystem destFS,
			TransferBehaviors tb, Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, IllegalFileAttributeException,
			AccessDeniedException {
		if (ensureDestinationIsDirectory(destFS, tb, dir)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS);
			try {
				destFS.setAttributes(dir, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		} else {
			try {
				destFS.createDirectory(dir, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		}
	}

	/**
	 * <ul>
	 * <li>if the destination path exists and is a file : it will be deleted ;</li>
	 * <li>if the destination path exists and is a link : it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param fs
	 *            is the file system where the operation will be performed.
	 * @param tb
	 *            specifies what to do if the file already exists.
	 * @param path
	 *            is the path of the directory to validate.
	 * 
	 * @return <tt>true</tt> if the destination path exists and is a directory
	 *         (no follow link), meaning it is not necessary to create such
	 *         directory, or <tt>false</tt> otherwise, meaning it is now safe to
	 *         create such directory.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if the given file exists, is not a directory and tb contains
	 *             {@link TransferBehavior#FAIL_IF_DIFFRENT_TYPE}.
	 * @throws IOException
	 */
	protected static boolean ensureDestinationIsDirectory(FileSystem fs,
			TransferBehaviors tb, Path path) throws IOException,
			FileAlreadyExistsException, AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			return true;
		}
		if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
			throw new WrapperFileAlreadyExistsException(path);
		}
		fs.deleteIfExists(path);
		return false;
	}

	protected static void createSymbolicLink(FileSystem destFS,
			TransferBehaviors tb, Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		if (ensureDestinationIsSymbolicLink(destFS, tb, link, target)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_LINK_ALREADY_EXISTS);
			try {
				destFS.setAttributes(link, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		} else {
			try {
				destFS.createSymbolicLink(link, target, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		}
	}

	/**
	 * <ul>
	 * <li>if the destination path exists and is a directory : it will be
	 * deleted (recurs) ;</li>
	 * <li>if the destination path exists and is a file : it will be deleted ;</li>
	 * <li>if the destination path exists and is a link and is target is not
	 * valid : it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param fs
	 *            is the file system where the operation will be performed.
	 * @param tb
	 *            specifies what to do if the file already exists.
	 * @param path
	 *            is the path of the destination link to validate.
	 * @param target
	 *            is the expected target.
	 * 
	 * @return <tt>true</tt> if the destination path exists, is a link (no
	 *         follow link) and point to the correct target, meaning it is not
	 *         necessary to create such symbolic link or <tt>false</tt>
	 *         otherwise, meaning it is now safe to create such symbolic link.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if the given file exists, is not a symbolic link and tb
	 *             contains {@link TransferBehavior#FAIL_IF_DIFFRENT_TYPE}.
	 * @throws IOException
	 */
	public static boolean ensureDestinationIsSymbolicLink(FileSystem fs,
			TransferBehaviors tb, Path path, Path target) throws IOException,
			FileAlreadyExistsException, AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperDirectoryAlreadyExistsException(path);
			}
			fs.deleteDirectory(path);
		} else {
			if (destfileAttrs.isSymbolicLink()) {
				Path destTarget = destfileAttrs.getLinkTarget();
				if (target.toString().equals(destTarget.toString())) {
					return true;
				}
			} else {
				// if file
				if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
					throw new WrapperSymbolicLinkAlreadyExistsException(path);
				}
			}
			fs.deleteIfExists(path);
		}
		return false;
	}

	public static void transformRegularFile(TransferableFileSystem fs,
			TransferBehaviors tb, Path source, EnhancedFileAttributes srcAttrs,
			Path dest, FileAttribute<?>[] destAttrs) throws IOException,
			NoSuchFileException, FileAlreadyExistsException,
			InterruptedIOException, AccessDeniedException {
		/*
		 * This call is important because it will remove the destination file
		 * (prior to copy) if it a link or a directory.
		 */
		if (TransferHelper.ensureDestinationIsRegularFile(fs, tb, srcAttrs,
				dest)) {
			/*
			 * should never go there cause source (template) have a different
			 * size than dest (result).
			 */
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS);
			try {
				fs.setAttributes(dest, destAttrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		} else {
			try {
				fs.transformRegularFile(source, dest, destAttrs);
			} catch (TemplatingException Ex) {
				throw new IOException(null, Ex);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		}
	}

	public static void transferRegularFile(TransferableFileSystem fs,
			TransferBehaviors tb, Path source, EnhancedFileAttributes srcAttrs,
			Path dest, FileAttribute<?>[] destAttrs) throws IOException,
			FileAlreadyExistsException, InterruptedIOException,
			AccessDeniedException {
		if (TransferHelper.ensureDestinationIsRegularFile(fs, tb, srcAttrs,
				dest)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS);
			try {
				fs.setAttributes(dest, destAttrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		} else {
			try {
				fs.transferRegularFile(source, dest, destAttrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.getUserFriendlyStackTrace());
			}
		}
	}

	/**
	 * <ul>
	 * <li>if the destination path exists and is a directory : it will be
	 * deleted (recurs) ;</li>
	 * <li>if the destination path exists and is a link : it will be deleted ;</li>
	 * <li>if the destination path exists and is not 'equals' to the source file
	 * (regarding the given transfer behavior) : it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param fs
	 *            is the file system where the operation will be performed.
	 * @param tb
	 *            specifies what to do if the file already exists.
	 * @param srcAttrs
	 *            are the source file attributes.
	 * @param dest
	 *            is the path of the destination file to validate.
	 * 
	 * @return <tt>true</tt>, if the source path is a file (no follow link)
	 *         'equals' to the destination file, meaning it is not necessary to
	 *         transfer such file, or <tt>false</tt> otherwise, meaning it is
	 *         now safe to transfer such file.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if the given file exists, is not a regular file and tb
	 *             contains {@link TransferBehavior#FAIL_IF_DIFFRENT_TYPE}.
	 * @throws IOException
	 */
	public static boolean ensureDestinationIsRegularFile(FileSystem fs,
			TransferBehaviors tb, EnhancedFileAttributes srcAttrs, Path dest)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(dest);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperDirectoryAlreadyExistsException(dest);
			}
			fs.deleteDirectory(dest);
		} else {
			if (!destfileAttrs.isSymbolicLink()) {
				if (!shouldTranferFile(tb, srcAttrs, destfileAttrs)) {
					return true;
				}
			} else {
				// if link
				if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
					throw new WrapperSymbolicLinkAlreadyExistsException(dest);
				}
			}
			fs.deleteIfExists(dest);
		}
		return false;
	}

	/**
	 * @param tb
	 *            is the desired transfer behavior.
	 * @param srcAttrs
	 *            are the source file attributes.
	 * @param destfileAttrs
	 *            are the destination file attributes.
	 * 
	 * @return <tt>true</tt> if the given destination file should be transfered,
	 *         or <tt>false</tt> otherwise. More formally :
	 *         <ul>
	 *         <li>return <tt>true</tt> if the desired transfer behavior
	 *         contains {@link TransferBehavior#FORCE_OVERWRITE} ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior
	 *         contains {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         source file size is not equal to the destination file size ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior
	 *         contains {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         source file size is equal to the destination file size and the
	 *         source file last modification time is newer than the destination
	 *         file last modification time ;</li>
	 *         <li>return <tt>false</tt> otherwise ;</li>
	 *         </ul>
	 * 
	 * @throws IOException
	 */
	private static boolean shouldTranferFile(TransferBehaviors tb,
			EnhancedFileAttributes srcAttrs, EnhancedFileAttributes destAttrs)
			throws IOException {
		if (tb.contains(TransferBehavior.FORCE_OVERWRITE)) {
			return true;
		}
		return srcAttrs.size() != destAttrs.size()
				|| srcAttrs.lastModifiedTime().compareTo(
						destAttrs.lastModifiedTime()) > 0;
	}

}