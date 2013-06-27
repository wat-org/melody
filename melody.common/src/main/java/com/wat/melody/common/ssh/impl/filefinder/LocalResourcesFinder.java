package com.wat.melody.common.ssh.impl.filefinder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wat.melody.common.ssh.types.filesfinder.ResourceSpecification;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesUpdater;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LocalResourcesFinder {

	public static List<LocalResource> findResources(ResourcesSelector rs)
			throws IOException {
		if (rs.getLocalBaseDir() == null) {
			return new ArrayList<LocalResource>();
		}
		List<LocalResource> rrs = new Finder(rs).findFiles();
		for (ResourcesUpdater ru : rs.getResourcesUpdaters()) {
			ru.update(rrs);
		}
		return rrs;
	}

}

class Finder extends SimpleFileVisitor<Path> {

	private ResourcesSelector _rs;
	private Path _topdir;
	private final PathMatcher _matcher;
	private final List<LocalResource> _localResources;

	public Finder(ResourcesSelector rs) {
		super();
		if (rs == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ ResourcesSelector.class.getCanonicalName() + ".");
		}
		if (rs.getLocalBaseDir() == null) {
			throw new IllegalArgumentException("invalid arg : the given "
					+ ResourcesSelector.LOCAL_BASEDIR_ATTR + " is null.");
		}
		if (rs.getMatch() == null) {
			throw new IllegalArgumentException("invalid arg : the given "
					+ ResourceSpecification.MATCH_ATTR + " is null.");
		}
		_localResources = new ArrayList<LocalResource>();
		_rs = rs;
		_topdir = Paths.get(rs.getLocalBaseDir().getAbsolutePath()).normalize();
		String match = _topdir + SysTool.FILE_SEPARATOR + rs.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + match.replaceAll("\\\\", "\\\\\\\\");
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);
	}

	public List<LocalResource> findFiles() throws IOException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		Files.walkFileTree(Paths.get(_rs.getLocalBaseDir().getAbsolutePath())
				.normalize(), set, Integer.MAX_VALUE, this);
		return _localResources;
	}

	private void matches(Path path, BasicFileAttributes attrs) {
		if (path != null && _matcher.matches(path)) {
			// include path parent til topdir is reacher
			// TODO : include path parent til topdir is reacher
			// include path
			_localResources.add(new LocalResource(path, _rs));
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		matches(file, attrs);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		LocalResource sr = new LocalResource(dir, _rs);
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
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		return FileVisitResult.CONTINUE;
	}

}