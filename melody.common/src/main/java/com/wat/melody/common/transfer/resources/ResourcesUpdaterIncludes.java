package com.wat.melody.common.transfer.resources;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.TransferableFile;
import com.wat.melody.common.transfer.finder.TransferablesTree;

/**
 * <p>
 * Include all {@link TransferableFile}s which have a path (see
 * {@link TransferableFile#getSourcePath()}) matching this object's source path.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesUpdaterIncludes extends ResourceSpecification implements
		ResourcesUpdater {

	private ResourcesSpecification _r;
	PathMatcher _matcher;

	public ResourcesUpdaterIncludes(ResourcesSpecification r) {
		super(r);
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
	public void update(TransferablesTree root, Transferable t) {
		/*
		 * Because the new {@link ResourceSpecification} can override
		 * destination path, we must 'move' the {@link Transferable} in the
		 * tree. In order to do that move, we remove it, change {@link
		 * ResourceSpecification}, and put it back at its new location.
		 */
		root.remove(t);
		t.setResourceSpecification(this);
		root.put(t);
	}

}