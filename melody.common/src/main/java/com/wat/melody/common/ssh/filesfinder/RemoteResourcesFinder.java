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

	public static List<Resource> findResources(ChannelSftp chan,
			ResourcesSpecification rspec) throws DownloaderException,
			SshSessionException {
		if (rspec == null) {
			return new ArrayList<Resource>();
		}
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}

		List<Resource> rs = new RemoteFinder(chan, rspec).findFiles();
		// List<RemoteResource> rrs = findMatchingResources(chan, rs);
		for (ResourcesUpdater ru : rspec.getResourcesUpdaters()) {
			ru.update(rs);
		}
		return rs;
	}

}

class RemoteFinder extends RemoteSimpleFileVisitor<Path> {

	private ChannelSftp _chan;
	private ResourcesSpecification _rspec;
	private Path _topdir;
	private final PathMatcher _matcher;
	private final List<Resource> _resources;

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
		_resources = new ArrayList<Resource>();
		_chan = chan;
		_rspec = rs;
		_topdir = Paths.get(rs.getSrcBaseDir()).normalize();
		String match = _topdir + SysTool.FILE_SEPARATOR + rs.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + match.replaceAll("\\\\", "\\\\\\\\");
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);
	}

	public List<Resource> findFiles() throws SshSessionException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		// will go into visitFileFailed if the path doesn't exists
		new RemoteFileTreeWalker(_chan, set, this, Integer.MAX_VALUE)
				.walk(Paths.get(_rspec.getSrcBaseDir()).normalize());
		return _resources;
	}

	private void matches(Path path, RemoteFileAttributes attrs) {
		if (path != null && _matcher.matches(path)) {
			/*
			 * TODO : include path parent til topdir is reached.
			 */
			// include path
			_resources.add(new Resource(path, attrs, _rspec));
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
		Resource sr = new Resource(dir, attrs, _rspec);
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