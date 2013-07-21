package com.wat.melody.common.transfer.resources;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.transfer.TransferableFile;
import com.wat.melody.common.transfer.Transferable;

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

	public ResourcesUpdaterExcludes(ResourcesSpecification r) {
		_r = r;
	}

	@Override
	public void update(List<Transferable> list) {
		String path = Paths.get(_r.getSrcBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (int i = list.size() - 1; i >= 0; i--) {
			Transferable r = list.get(i);
			if (matcher.matches(r.getSourcePath())) {
				list.remove(i);
			}
		}
	}

}