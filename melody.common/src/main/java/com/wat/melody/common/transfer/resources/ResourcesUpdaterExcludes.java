package com.wat.melody.common.transfer.resources;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.TransferableFile;

/**
 * <p>
 * Exclude all {@link TransferableFile}s which have a path (see
 * {@link TransferableFile#getSourcePath()}) matching this object's source path.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesUpdaterExcludes extends ResourceSelector implements
		ResourcesUpdater {

	private ResourcesSpecification _r;
	PathMatcher _matcher;

	public ResourcesUpdaterExcludes(ResourcesSpecification r) {
		_r = r;
	}

	@Override
	public String setMatch(String match) {
		String previous = super.setMatch(match);

		String path = Paths.get(_r.getSrcBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		_matcher = FileSystems.getDefault().getPathMatcher(pattern);

		return previous;
	}

	@Override
	public boolean isMatching(Path path) {
		return _matcher.matches(path);
	}

	@Override
	public void update(List<Transferable> list, Transferable t) {
		list.remove(t);
	}

}