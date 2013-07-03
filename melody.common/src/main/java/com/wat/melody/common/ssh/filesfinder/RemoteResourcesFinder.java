package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.remotefiletreewalker.RemoteFileAttributes;
import com.wat.melody.common.ssh.filesfinder.remotefiletreewalker.RemoteFileTreeWalker;
import com.wat.melody.common.ssh.filesfinder.remotefiletreewalker.RemoteSimpleFileVisitor;
import com.wat.melody.common.ssh.impl.downloader.DownloaderException;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class RemoteResourcesFinder {

	public static List<RemoteResource> findResources(ChannelSftp chan,
			ResourcesSpecification rs) throws DownloaderException,
			SshSessionException {
		if (rs == null) {
			return new ArrayList<RemoteResource>();
		}
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}

		List<RemoteResource> rrs = new RemoteFinder(chan, rs).findFiles();
		// List<RemoteResource> rrs = findMatchingResources(chan, rs);
		for (ResourcesUpdater ru : rs.getResourcesUpdaters()) {
			ru.update(rrs);
		}
		return rrs;
	}

}

class RemoteFinder extends RemoteSimpleFileVisitor<Path> {

	private ChannelSftp _chan;
	private ResourcesSpecification _rs;
	private Path _topdir;
	private final PathMatcher _matcher;
	private final List<RemoteResource> _remoteResources;

	public RemoteFinder(ChannelSftp chan, ResourcesSpecification rs) {
		super();
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		if (rs == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ ResourcesSpecification.class.getCanonicalName() + ".");
		}
		if (!chan.isConnected()) {
			throw new IllegalStateException("Channel must be a connected.");
		}
		_remoteResources = new ArrayList<RemoteResource>();
		_chan = chan;
		_rs = rs;
		_topdir = Paths.get(rs.getSrcBaseDir()).normalize();
		String match = _topdir + SysTool.FILE_SEPARATOR + rs.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + match.replaceAll("\\\\", "\\\\\\\\");
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);
	}

	public List<RemoteResource> findFiles() throws SshSessionException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		// will go into visitFileFailed if the path doesn't exists
		new RemoteFileTreeWalker(_chan, set, this, Integer.MAX_VALUE)
				.walk(Paths.get(_rs.getSrcBaseDir()).normalize());
		return _remoteResources;
	}

	private void matches(Path path, RemoteFileAttributes attrs) {
		if (path != null && _matcher.matches(path)) {
			/*
			 * TODO : include path parent til topdir is reached.
			 */
			// include path
			_remoteResources.add(new RemoteResource(path, attrs, _rs));
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, RemoteFileAttributes attrs)
			throws SshSessionException {
		matches(file, attrs);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			RemoteFileAttributes attrs) throws SshSessionException {
		RemoteResource sr = new RemoteResource(dir, attrs, _rs);
		matches(dir, attrs);
		if (sr.isSymbolicLink()) {
			switch (sr.getLinkOption()) {
			case COPY_LINKS:
				return FileVisitResult.CONTINUE;
			case KEEP_LINKS:
				return FileVisitResult.SKIP_SUBTREE;
			case COPY_UNSAFE_LINKS:
				if (sr.isSafeLink()) {
					return FileVisitResult.SKIP_SUBTREE;
				} else {
					return FileVisitResult.CONTINUE;
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, SshSessionException exc) {
		return FileVisitResult.CONTINUE;
	}

}