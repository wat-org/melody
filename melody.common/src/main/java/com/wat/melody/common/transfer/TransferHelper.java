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

	/**
	 * <p>
	 * Ensure the given path exists and is a directory (no follow link).
	 * </p>
	 * <ul>
	 * <li>if the destination path exists and is a regular file : it will be
	 * deleted ;</li>
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
	 * @throws FileAlreadyExistsException
	 *             if the given file exists, is not a directory (no follow link)
	 *             and tb contains
	 *             {@link TransferBehavior#FAIL_IF_DIFFRENT_TYPE}.
	 * @throws IOException
	 */
	protected static void ensureDirectory(FileSystem fs, TransferBehaviors tb,
			Path path, FileAttribute<?>... attrs) throws IOException,
			NoSuchFileException, FileAlreadyExistsException,
			IllegalFileAttributeException, AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(path);
		} catch (NoSuchFileException ignore) {
		}

		if (destfileAttrs == null) {
			fs.createDirectory(path);
		} else if (!destfileAttrs.isDirectory()
				|| destfileAttrs.isSymbolicLink()) {
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperFileAlreadyExistsException(path);
			}
			fs.deleteIfExists(path);
			fs.createDirectory(path);
		} else {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS);
		}

		try {
			fs.setAttributes(path, attrs);
		} catch (IllegalFileAttributeException Ex) {
			log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
					.getUserFriendlyStackTrace());
		}
	}

	/**
	 * <p>
	 * Ensure the given path exists and is a directory (follow link).
	 * </p>
	 * <ul>
	 * <li>if the destination path exists and is a regular file : it will be
	 * deleted ;</li>
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
	 * @throws FileAlreadyExistsException
	 *             if the given file exists, is not a directory (follow link)
	 *             and tb contains
	 *             {@link TransferBehavior#FAIL_IF_DIFFRENT_TYPE}.
	 * @throws IOException
	 */
	protected static void ensureDirectoryOrLInk(FileSystem fs,
			TransferBehaviors tb, Path path, FileAttribute<?>... attrs)
			throws IOException, AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(path);
		} catch (NoSuchFileException ignore) {
		}

		if (destfileAttrs == null) {
			fs.createDirectory(path);
		} else if (!destfileAttrs.isDirectory()) {
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperFileAlreadyExistsException(path);
			}
			fs.deleteIfExists(path);
			fs.createDirectory(path);
		} else {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS);
		}

		try {
			fs.setAttributes(path, attrs);
		} catch (IllegalFileAttributeException Ex) {
			log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
					.getUserFriendlyStackTrace());
		}
	}

	/**
	 * <p>
	 * Ensure the given path exists, is a link and point to the given target.
	 * </p>
	 * <ul>
	 * <li>if the destination path exists and is a directory : it will be
	 * deleted (recurs) ;</li>
	 * <li>if the destination path exists and is a regular file : it will be
	 * deleted ;</li>
	 * <li>if the destination path exists, is a link and is target is not valid
	 * : it will be deleted ;</li>
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
	protected static void ensureSymbolicLink(FileSystem fs,
			TransferBehaviors tb, Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = fs.readAttributes(link);
		} catch (NoSuchFileException Ex) {
		}

		if (destfileAttrs == null) {
			fs.createSymbolicLink(link, target);
		} else if (destfileAttrs.isDirectory()
				&& !destfileAttrs.isSymbolicLink()) {
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperDirectoryAlreadyExistsException(link);
			}
			fs.deleteDirectory(link);
			fs.createSymbolicLink(link, target);
		} else if (destfileAttrs.isSymbolicLink()) {
			Path destTarget = destfileAttrs.getLinkTarget();
			if (!target.toString().equals(destTarget.toString())) {
				fs.deleteIfExists(link);
				fs.createSymbolicLink(link, target);
			}
		} else {
			// if file
			if (tb.contains(TransferBehavior.FAIL_IF_DIFFRENT_TYPE)) {
				throw new WrapperSymbolicLinkAlreadyExistsException(link);
			}
			fs.deleteIfExists(link);
			fs.createSymbolicLink(link, target);
		}

		try {
			fs.setAttributes(link, attrs);
		} catch (IllegalFileAttributeException Ex) {
			log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
					.getUserFriendlyStackTrace());
		}
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
		if (TransferHelper.ensureIsRegularFile(fs, tb, srcAttrs, dest)) {
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
		if (TransferHelper.ensureIsRegularFile(fs, tb, srcAttrs, dest)) {
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
	public static boolean ensureIsRegularFile(FileSystem fs,
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