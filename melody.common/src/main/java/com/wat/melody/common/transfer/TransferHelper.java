package com.wat.melody.common.transfer;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferHelper {

	private static Logger log = LoggerFactory.getLogger(TransferHelper.class);

	protected static void createDirectory(FileSystem destFS, Path dir,
			FileAttribute<?>... attrs) throws IOException {
		/*
		 * TODO : introduce a FORCE option, which will delete existing if true
		 */
		if (ensureDestinationIsDirectory(destFS, dir)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS);
			try {
				destFS.setAttributes(dir, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		} else {
			try {
				destFS.createDirectory(dir, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
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
	 * @param path
	 *            is the path of the directory to validate.
	 * 
	 * @return <tt>true</tt> if the destination path exists and is a directory
	 *         (no follow link), meaning it is not necessary to create such
	 *         directory, or <tt>false</tt> otherwise, meaning it is now safe to
	 *         create such directory.
	 * 
	 * @throws IOException
	 */
	protected static boolean ensureDestinationIsDirectory(FileSystem destFS,
			Path path) throws IOException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = destFS.readAttributes(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			return true;
		}
		destFS.deleteIfExists(path);
		return false;
	}

	protected static void createSymbolicLink(FileSystem destFS, Path link,
			Path target, FileAttribute<?>... attrs) throws IOException {
		if (ensureDestinationIsSymbolicLink(destFS, link, target)) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_LINK_ALREADY_EXISTS);
			try {
				destFS.setAttributes(link, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		} else {
			try {
				destFS.createSymbolicLink(link, target, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
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
	 * @throws IOException
	 */
	public static boolean ensureDestinationIsSymbolicLink(FileSystem destFS,
			Path path, Path target) throws IOException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = destFS.readAttributes(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			destFS.deleteDirectory(path);
		} else {
			if (destfileAttrs.isSymbolicLink()) {
				Path destTarget = destfileAttrs.getLinkTarget();
				if (target.toString().equals(destTarget.toString())) {
					return true;
				}
			}
			destFS.deleteIfExists(path);
		}
		return false;
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
	 * @param sourcefileAttrs
	 *            are the source file attributes.
	 * @param path
	 *            is the path of the destination file to validate.
	 * 
	 * @return <tt>true</tt>, if the source path is a file (no follow link)
	 *         'equals' to the destination file, meaning it is not necessary to
	 *         transfer such file, or <tt>false</tt> otherwise, meaning it is
	 *         now safe to transfer such file.
	 * 
	 * @throws IOException
	 */
	public static boolean ensureDestinationIsRegularFile(FileSystem destFS,
			EnhancedFileAttributes sourcefileAttrs, Path path,
			TransferBehavior tb) throws IOException {
		EnhancedFileAttributes destfileAttrs = null;
		try {
			destfileAttrs = destFS.readAttributes(path);
		} catch (NoSuchFileException Ex) {
			return false;
		}
		if (destfileAttrs.isDirectory() && !destfileAttrs.isSymbolicLink()) {
			destFS.deleteDirectory(path);
		} else {
			if (!destfileAttrs.isSymbolicLink()
					&& !shouldTranferFile(sourcefileAttrs, destfileAttrs, tb)) {
				return true;
			}
			destFS.deleteIfExists(path);
		}
		return false;
	}

	/**
	 * @param sourcefileAttrs
	 *            are the source file attributes.
	 * @param destfileAttrs
	 *            are the destination file attributes.
	 * @param tb
	 *            is the desired transfer behavior.
	 * 
	 * @return <tt>true</tt> if the given destination file should be transfered,
	 *         or <tt>false</tt> otherwise. More formally :
	 *         <ul>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#FORCE_OVERWRITE} ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         source file size is not equal to the destination file size ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         source file size is equal to the destination file size and the
	 *         source file last modification time is newer than the destination
	 *         file last modification time ;</li>
	 *         <li>return <tt>false</tt> otherwise ;</li>
	 *         </ul>
	 * 
	 * @throws IOException
	 */
	private static boolean shouldTranferFile(
			EnhancedFileAttributes sourcefileAttrs,
			EnhancedFileAttributes destinationfileAttrs, TransferBehavior tb)
			throws IOException {
		if (tb == TransferBehavior.FORCE_OVERWRITE) {
			return true;
		}
		return sourcefileAttrs.size() != destinationfileAttrs.size()
				|| sourcefileAttrs.lastModifiedTime().compareTo(
						destinationfileAttrs.lastModifiedTime()) > 0;
	}

}