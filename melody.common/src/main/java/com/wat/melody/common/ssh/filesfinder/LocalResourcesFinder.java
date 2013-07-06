package com.wat.melody.common.ssh.filesfinder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LocalResourcesFinder {

	public static List<Resource> findResources(ResourcesSpecification rspec)
			throws IOException {
		if (rspec == null) {
			return new ArrayList<Resource>();
		}
		List<Resource> rs = new Finder(rspec).findFiles();
		for (ResourcesUpdater ru : rspec.getResourcesUpdaters()) {
			ru.update(rs);
		}
		return rs;
	}

}

class Finder extends SimpleFileVisitor<Path> {

	private ResourcesSpecification _rspec;
	private Path _topdir;
	private final PathMatcher _matcher;
	private final List<Resource> _resources;

	public Finder(ResourcesSpecification rs) {
		super();
		if (rs == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ ResourcesSpecification.class.getCanonicalName() + ".");
		}
		_resources = new ArrayList<Resource>();
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

	public List<Resource> findFiles() throws IOException {
		Set<FileVisitOption> set = new HashSet<FileVisitOption>();
		set.add(FileVisitOption.FOLLOW_LINKS);
		// will go into visitFileFailed if the path doesn't exists
		Files.walkFileTree(Paths.get(_rspec.getSrcBaseDir()).normalize(), set,
				Integer.MAX_VALUE, this);
		return _resources;
	}

	private void matches(Path path, EnhancedFileAttributes attrs) {
		if (path != null && _matcher.matches(path)) {
			/*
			 * TODO : include path parent til topdir is reached.
			 */
			// include path
			_resources.add(new Resource(path, attrs, _rspec));
		}
	}

	private EnhancedFileAttributes newLocalFileAttributes(Path path,
			BasicFileAttributes attrs) throws IOException {
		Path target = null;
		BasicFileAttributes targetAttrs = null;
		if (Files.isSymbolicLink(path)) {
			target = Files.readSymbolicLink(path);
			// necessary because attrs contains the link's target attributes!
			targetAttrs = attrs;
			attrs = Files.readAttributes(path, BasicFileAttributes.class,
					LinkOption.NOFOLLOW_LINKS);
		}
		return new LocalFileAttributes(attrs, target, targetAttrs);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		matches(file, newLocalFileAttributes(file, attrs));
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		EnhancedFileAttributes hattrs = newLocalFileAttributes(dir, attrs);
		Resource lr = new Resource(dir, hattrs, _rspec);
		matches(dir, hattrs);
		if (lr.isSymbolicLink()) {
			switch (lr.getLinkOption()) {
			case COPY_LINKS:
				return FileVisitResult.CONTINUE;
			case KEEP_LINKS:
				return FileVisitResult.SKIP_SUBTREE;
			case COPY_UNSAFE_LINKS:
				if (lr.isSafeLink()) {
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