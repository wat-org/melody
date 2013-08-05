package com.wat.melody.common.transfer.finder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.files.EnhancedFileTreeWalker;
import com.wat.melody.common.files.EnhancedFileVisitor;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.TransferableFile;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.common.transfer.resources.ResourcesUpdater;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferablesFinder {

	public static TransferablesTree find(FileSystem fs,
			List<ResourcesSpecification> rss) throws IOException {
		if (fs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		List<Transferable> found = new ArrayList<Transferable>();
		if (rss != null) {
			for (ResourcesSpecification rspec : rss) {
				List<Transferable> rs = new Finder(fs, rspec).findFiles();
				found.removeAll(rs); // remove duplicated
				found.addAll(rs);
			}
		}
		return TransferablesTree.build(found);
	}

}

class Finder extends EnhancedFileVisitor<Path> {

	private FileSystem _fs;
	private ResourcesSpecification _rspec;
	private Path _srcBaseDir;
	private List<Transferable> _resources;
	private final PathMatcher _matcher;

	public Finder(FileSystem fs, ResourcesSpecification rs) {
		super();
		if (fs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		if (rs == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ ResourcesSpecification.class.getCanonicalName() + ".");
		}
		_fs = fs;
		_rspec = rs;
		_srcBaseDir = Paths.get(rs.getSrcBaseDir()).normalize();
		_resources = new ArrayList<Transferable>();
		String match = _srcBaseDir + SysTool.FILE_SEPARATOR + rs.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + match.replaceAll("\\\\", "\\\\\\\\");
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);
	}

	public List<Transferable> findFiles() throws IOException {
		// will go into visitFileFailed if the path doesn't exists
		new EnhancedFileTreeWalker(_fs,
				EnumSet.<FileVisitOption> of(FileVisitOption.FOLLOW_LINKS),
				this, Integer.MAX_VALUE).walk(_srcBaseDir);
		return _resources;
	}

	private void matches(TransferableFile t) {
		Path path = t.getSourcePath();
		if (path == null || !_matcher.matches(path)) {
			return;
		}
		// if the transferable is matching => store it
		_resources.add(t);
		
		// find the last matching updater
		ResourcesUpdater winner = null;
		for (ResourcesUpdater ru : _rspec.getResourcesUpdaters()) {
			if (ru.isMatching(path)) {
				winner = ru;
			}
		}
		// apply the found updater
		if (winner != null) {
			winner.update(_resources, t);
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, EnhancedFileAttributes attrs)
			throws IOException {
		TransferableFile sr = new TransferableFile(file, attrs, _rspec);
		matches(sr);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			EnhancedFileAttributes attrs) throws IOException {
		TransferableFile sr = new TransferableFile(dir, attrs, _rspec);
		matches(sr);
		if (sr.linkShouldBeConvertedToFile()) {
			return FileVisitResult.CONTINUE;
		} else {
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		return FileVisitResult.CONTINUE;
	}

}