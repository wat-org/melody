package com.wat.melody.common.ssh.filesfinder.remotefiletreewalker;

import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.impl.SftpHelper;

/**
 * <p>
 * Simple remote file tree walker that works in a similar manner to nftw(3C).
 * </p>
 * 
 * @see Files#walkFileTree
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteFileTreeWalker {

	private final ChannelSftp _chan;
	private final boolean _followLinks;
	private final RemoteFileVisitor<? super Path> _visitor;
	private final int _maxDepth;

	public RemoteFileTreeWalker(ChannelSftp chan, Set<FileVisitOption> options,
			RemoteFileVisitor<? super Path> visitor, int maxDepth) {
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		if (visitor == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ RemoteFileVisitor.class.getCanonicalName() + "<? super "
					+ Path.class.getCanonicalName() + ">.");
		}
		if (maxDepth < 0) {
			throw new IllegalArgumentException(maxDepth + ": Not accepted. "
					+ "Must be a positive integer or 0.");
		}

		boolean fl = false;
		if (options != null) {
			for (FileVisitOption option : options) {
				if (option == null) {
					continue;
				} else if (option == FileVisitOption.FOLLOW_LINKS) {
					fl = true;
				} else {
					throw new RuntimeException("Should not get here");
				}
			}
		}
		_chan = chan;
		_followLinks = fl;
		_visitor = visitor;
		_maxDepth = maxDepth;
	}

	/**
	 * Walk file tree starting at the given file
	 */
	public void walk(Path start) throws SshSessionException {
		FileVisitResult result = walk(start, 0,
				new ArrayList<AncestorDirectory>());
		Objects.requireNonNull(result, "FileVisitor returned null");
	}

	/**
	 * @param path
	 *            the directory to visit
	 * @param depth
	 *            depth remaining
	 * @param ancestors
	 *            use when cycle detection is enabled
	 */
	private FileVisitResult walk(Path path, int depth,
			List<AncestorDirectory> ancestors) throws SshSessionException {
		SftpATTRS attrs = null;
		String unixPath = SftpHelper.convertToUnixPath(path);

		// attempt to get attributes of file. If fails and we are following
		// links then a link target might not exist so get attributes of link
		try {
			attrs = SftpHelper.scp_lstat(_chan, unixPath);
		} catch (SshSessionException Ex) {
			return _visitor.visitFileFailed(path, Ex);
		}
		if (attrs == null) {
			return _visitor.visitFileFailed(path, new SshSessionException(
					unixPath + ": No such file"));
		}

		// if this is a link, retrieve link's target
		String unixTarget = null;
		SftpATTRS targetAttrs = null;
		if (attrs.isLink()) {
			try {
				unixTarget = SftpHelper.scp_readlink(_chan, unixPath);
				targetAttrs = SftpHelper.scp_stat(_chan, unixPath);
			} catch (SshSessionException Ex) {
				return _visitor.visitFileFailed(path, Ex);
			}
		}

		// at maximum depth or file is not a directory
		if ((depth >= _maxDepth || !attrs.isDir())
				&& !(_followLinks && targetAttrs != null && targetAttrs.isDir())) {
			return _visitor.visitFile(path, new RemoteFileAttributes(attrs,
					unixTarget, targetAttrs));
		}

		// check for cycles when following links
		if (_followLinks) {
			// if this directory and ancestor has a file key then we compare
			// them; otherwise we use less efficient isSameFile test.
			for (AncestorDirectory ancestor : ancestors) {
				if (ancestor.file().equals(path)) {
					// cycle detected
					return _visitor.visitFileFailed(path,
							new SshSessionException(unixPath
									+ ": Cycle detected"));
				}
			}

			ancestors.add(new AncestorDirectory(path));
		}

		// visit directory
		try {
			Vector<LsEntry> stream;
			FileVisitResult result;

			// open the directory
			try {
				stream = SftpHelper.scp_ls(_chan, unixPath);
			} catch (SshSessionException Ex) {
				return _visitor.visitFileFailed(path, Ex);
			}

			// the exception notified to the postVisitDirectory method
			ConsolidatedException causes = new ConsolidatedException(unixPath
					+ ": errors occured while visiting directory.");

			// invoke preVisitDirectory and then visit each entry
			result = _visitor.preVisitDirectory(path, new RemoteFileAttributes(
					attrs, unixTarget, targetAttrs));
			if (result != FileVisitResult.CONTINUE) {
				return result;
			}

			for (LsEntry entry : stream) {
				if (entry.getFilename().equals(".")
						|| entry.getFilename().equals("..")) {
					continue;
				}
				try {
					result = walk(
							Paths.get(unixPath + "/" + entry.getFilename()),
							depth + 1, ancestors);

					// returning null will cause NPE to be thrown
					if (result == null || result == FileVisitResult.TERMINATE) {
						return result;
					}
					// skip remaining siblings in this directory
					if (result == FileVisitResult.SKIP_SIBLINGS) {
						break;
					}
				} catch (SshSessionException Ex) {
					causes.addCause(Ex);
				}
			}

			// invoke postVisitDirectory last
			return _visitor.postVisitDirectory(path,
					causes.countCauses() == 0 ? null : new SshSessionException(
							causes));

		} finally {
			// remove key from trail if doing cycle detection
			if (_followLinks) {
				ancestors.remove(ancestors.size() - 1);
			}
		}
	}

	private static class AncestorDirectory {
		private final Path _dir;

		AncestorDirectory(Path dir) {
			_dir = dir;
		}

		Path file() {
			return _dir;
		}
	}

}