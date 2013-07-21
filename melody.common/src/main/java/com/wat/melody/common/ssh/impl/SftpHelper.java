package com.wat.melody.common.ssh.impl;

import java.nio.file.Path;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SftpHelper {

	public static void scp_put(ChannelSftp chan, String source, String dest)
			throws SshSessionException {
		try {
			chan.put(source, dest);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SfptEx_PUT, source,
					dest), Ex);
		}
	}

	public static void scp_get(ChannelSftp chan, String source, String dest)
			throws SshSessionException {
		try {
			chan.get(source, dest);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SfptEx_GET, source,
					dest), Ex);
		}
	}

	public static void scp_mkdir(ChannelSftp chan, String dir)
			throws SshSessionException {
		try {
			chan.mkdir(dir);
		} catch (SftpException Ex) {
			// creation fails because the dir already exists => no error
			if (scp_lexists(chan, dir)) {
				return;
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_MKDIR, dir),
					Ex);
		}
	}

	public static void scp_rmdir(ChannelSftp chan, String dir)
			throws SshSessionException {
		try {
			chan.rmdir(dir);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_RMDIR, dir),
					Ex);
		}
	}

	/**
	 * @param chan
	 * @param file
	 * @throws SshSessionException
	 *             if an error occurred. Or if the file doesn't exists.
	 */
	public static void scp_rm(ChannelSftp chan, String file)
			throws SshSessionException {
		try {
			chan.rm(file);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_RM, file),
					Ex);
		}
	}

	/**
	 * @param chan
	 * @param file
	 * 
	 * @return <tt>true</tt> if the file/link was deleted, <tt>false</tt> if the
	 *         file/link didn't exists.
	 * 
	 * @throws SshSessionException
	 *             if an error occurred.
	 */
	public static boolean scp_rmIfExists(ChannelSftp chan, String file)
			throws SshSessionException {
		try {
			chan.rm(file);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_RM, file),
					Ex);
		}
		return true;
	}

	public static String scp_readlink(ChannelSftp chan, String link)
			throws SshSessionException {
		try {
			return chan.readlink(link);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return null;
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_READLINK,
					link), Ex);
		}
	}

	public static String scp_realpath(ChannelSftp chan, String link)
			throws SshSessionException {
		try {
			return chan.realpath(link);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return null;
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_REALPATH,
					link), Ex);
		}
	}

	public static void scp_symlink(ChannelSftp chan, String target, String link)
			throws SshSessionException {
		try {
			chan.symlink(target, link);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_LN, target,
					link), Ex);
		}
	}

	public static void scp_chmod(ChannelSftp chan, Modifiers modifiers,
			String path) throws SshSessionException {
		try {
			chan.chmod(modifiers.toInt(), path);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_CHMOD,
					modifiers, path), Ex);
		}
	}

	public static void scp_chgrp(ChannelSftp chan, GroupID group, String path)
			throws SshSessionException {
		try {
			chan.chgrp(group.toInt(), path);
		} catch (SftpException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_CHGRP,
					group, path), Ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static Vector<LsEntry> scp_ls(ChannelSftp chan, String path)
			throws SshSessionException {
		try {
			return chan.ls(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return new Vector<LsEntry>();
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_LS, path),
					Ex);
		}
	}

	public static SftpATTRS scp_lstat(ChannelSftp chan, String path)
			throws SshSessionException {
		try {
			return chan.lstat(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return null;
			}
			throw new SshSessionException(
					Msg.bind(Messages.SftpEx_LSTAT, path), Ex);
		}
	}

	public static SftpATTRS scp_stat(ChannelSftp chan, String path)
			throws SshSessionException {
		try {
			return chan.stat(path);
		} catch (SftpException Ex) {
			if (Ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return null;
			}
			throw new SshSessionException(Msg.bind(Messages.SftpEx_STAT, path),
					Ex);
		}
	}

	public static boolean scp_lexists(ChannelSftp chan, String path)
			throws SshSessionException {
		return scp_lstat(chan, path) != null;
	}

	public static boolean scp_exists(ChannelSftp chan, String path)
			throws SshSessionException {
		return scp_stat(chan, path) != null;
	}

	public static void scp_rmdirs(ChannelSftp chan, String dir)
			throws SshSessionException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory Path, relative or absolute).");
		}
		for (LsEntry entry : scp_ls(chan, dir)) {
			if (entry.getAttrs().isDir()) {
				if (entry.getFilename().equals(".")
						|| entry.getFilename().equals("..")) {
					continue;
				}
				scp_rmdirs(chan, dir + "/" + entry.getFilename());
			} else {
				scp_rm(chan, dir + "/" + entry.getFilename());
			}
		}
		scp_rmdir(chan, dir);
	}

	public static void scp_mkdirs(ChannelSftp chan, Path dir)
			throws SshSessionException {
		if (dir.toString().length() == 0 || dir.getNameCount() < 1) {
			return;
		}
		String unixDir = convertToUnixPath(dir);
		try {
			scp_mkdir(chan, unixDir);
			return;
		} catch (SshSessionException Ex) {
			// if the top first dir cannot be created => raise an error
			if (dir.getNameCount() <= 1) {
				throw Ex;
			}
		}
		// if dir cannot be created => create its parent
		Path parent = null;
		try {
			parent = dir.resolve("..").normalize();
			scp_mkdirs(chan, parent);
		} catch (SshSessionException Ex) {
			throw new SshSessionException(Msg.bind(Messages.SftpEx_MKDIR,
					unixDir), Ex);
		}
		scp_mkdir(chan, unixDir);
	}

	public static String convertToUnixPath(String path) {
		return path.replaceAll("\\\\", "/");
	}

	public static String convertToUnixPath(Path path) {
		return convertToUnixPath(path.toString());
	}

}