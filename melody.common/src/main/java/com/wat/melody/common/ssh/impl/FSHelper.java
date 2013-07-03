package com.wat.melody.common.ssh.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.remotefiletreewalker.RemoteFileAttributes;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class FSHelper {

	public static void ln(Path link, Path target) throws SshSessionException {
		try {
			Files.createSymbolicLink(link, target);
		} catch (IOException Ex) {
			throw new SshSessionException(Msg.bind(Messages.FSEx_LN, link,
					target), Ex);
		}
	}

	public static void mkdir(Path dir) throws SshSessionException {
		try {
			Files.createDirectory(dir);
		} catch (IOException Ex) {
			throw new SshSessionException(Msg.bind(Messages.FSEx_MKDIR, dir),
					Ex);
		}
	}

	/**
	 * <ul>
	 * <li>if the local path exists and is a directory : it will be deleted
	 * (recurs) ;</li>
	 * <li>if the local path exists and is a file : it will be deleted ;</li>
	 * <li>if the local path exists and is a link and is target is not valid :
	 * it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param path
	 *            is the path of the local link to validate.
	 * @param target
	 *            is the expected target.
	 * 
	 * @return <tt>true</tt> if the local path exists, is a link (no follow
	 *         link) and point to the correct target, meaning it is not
	 *         necessary to create such symbolic link or <tt>false</tt>
	 *         otherwise, meaning it is now safe to create such symbolic link.
	 * 
	 * @throws IOException
	 */
	public static boolean ensureLink(Path target, Path path) throws IOException {
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return false;
		}
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			FileUtils.deleteDirectory(path.toFile());
		} else {
			if (Files.isSymbolicLink(path)) {
				Path localTarget = Files.readSymbolicLink(path);
				if (target.toString().equals(localTarget.toString())) {
					return true;
				}
			}
			Files.deleteIfExists(path);
		}
		// delete operation works ? or not (permission issue) ?
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException(Msg.bind(
					Messages.DownloadEx_ENSURE_LINK_FAILED, path));
		}
		return false;
	}

	/**
	 * <ul>
	 * <li>if the local path exists and is a file : it will be deleted ;</li>
	 * <li>if the local path exists and is a link : it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param path
	 *            is the path of the directory link to validate.
	 * 
	 * @return <tt>true</tt> if the local path exists and is a directory (no
	 *         follow link), meaning it is not necessary to create such
	 *         directory, or <tt>false</tt> otherwise, meaning it is now safe to
	 *         create such directory.
	 * 
	 * @throws IOException
	 */
	public static boolean ensureDir(Path path) throws IOException {
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return false;
		}
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			return true;
		}
		Files.deleteIfExists(path);
		// delete operation works ? or not (permission issue) ?
		// but another thread may have create this directory between ...
		/*
		 * BTW : it is not a good way to do this. The download process should
		 * remove everything first, then create directory tree, then create
		 * link, then download files.
		 */
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return false;
		}
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			return true;
		}
		throw new IOException(Msg.bind(Messages.DownloadEx_ENSURE_DIR_FAILED,
				path));
	}

	/**
	 * <ul>
	 * <li>if the local path exists and is a directory : it will be deleted
	 * (recurs) ;</li>
	 * <li>if the local path exists and is a link : it will be deleted ;</li>
	 * <li>if the local path exists and is not 'equals' to the remote file
	 * (regarding the given transfer behavior) : it will be deleted ;</li>
	 * <li>if the deletion failed : throws an exception ;</li>
	 * </ul>
	 * 
	 * @param remotefileAttrs
	 *            are the remote file attributes.
	 * @param path
	 *            is the path of the local file to validate.
	 * 
	 * @return <tt>true</tt>, if the local path is a file (no follow link)
	 *         'equals' to the remote file, meaning it is not necessary to
	 *         download such file, or <tt>false</tt> otherwise, meaning it is
	 *         now safe to download such file.
	 * 
	 * @throws IOException
	 */
	public static boolean ensureFile(RemoteFileAttributes remotefileAttrs,
			Path path, TransferBehavior tb) throws IOException,
			SshSessionException {
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return false;
		}
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			FileUtils.deleteDirectory(path.toFile());
		} else {
			if (!Files.isSymbolicLink(path)
					&& !shouldTranferFile(remotefileAttrs, path, tb)) {
				return true;
			}
			Files.deleteIfExists(path);
		}
		// delete operation works ? or not (permission issue) ?
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException(Msg.bind(
					Messages.DownloadEx_ENSURE_FILE_FAILED, path));
		}
		return false;
	}

	/**
	 * @param remotefileAttrs
	 *            are the remote file attributes.
	 * @param path
	 *            is the local file to compare.
	 * @param tb
	 *            is the desired transfer behavior.
	 * 
	 * @return <tt>true</tt> if the given remote file should be transfered, or
	 *         <tt>false</tt> otherwise. More formally :
	 *         <ul>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#FORCE_OVERWRITE} ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         remote file size is not equal to the local file size ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_SRC_NEWER} and the
	 *         remote file size is equal to the local file size and the remote
	 *         file last modification time is newer than the local file last
	 *         modification time ;</li>
	 *         <li>return <tt>false</tt> otherwise ;</li>
	 *         </ul>
	 */
	private static boolean shouldTranferFile(
			RemoteFileAttributes remotefileAttrs, Path path, TransferBehavior tb) {
		if (tb == TransferBehavior.FORCE_OVERWRITE) {
			return true;
		}
		File f = path.toFile();
		return remotefileAttrs.getSize() != f.length()
				|| remotefileAttrs.getMTime() > f.lastModified() / 1000;
	}

}