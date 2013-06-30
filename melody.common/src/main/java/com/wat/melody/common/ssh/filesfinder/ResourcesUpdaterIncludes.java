package com.wat.melody.common.ssh.filesfinder;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * Include all {@link Resource}s which have a path (see
 * {@link Resource#getPath()}) matching this object's source path.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesUpdaterIncludes extends ResourceSpecification implements
		ResourcesUpdater {

	private ResourcesSpecification _r;

	public ResourcesUpdaterIncludes(ResourcesSpecification r) {
		super(r);
		_r = r;
	}

	@Override
	public void update(List<? extends Resource> list) {
		String path = Paths.get(_r.getSrcBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (Resource r : list) {
			if (matcher.matches(r.getPath())) {
				r.setResourceSpecification(this);
			}
		}
	}

}