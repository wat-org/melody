package com.wat.melody.common.ssh.types;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * A {@link Resources} is especially design to find files and directories, based
 * on multiple {@link ResourceMatcher} include/exclude conditions.
 * </p>
 * 
 * <p>
 * A {@link Resources} is an {@link ITask} nested element, which can contains
 * include/exclude {@link ResourceMatcher}. It is itself a
 * {@link ResourceMatcher}. All attributes defined in it are passed as default
 * values for the inner include and exclude {@link ResourceMatcher}.
 * </p>
 * 
 * <p>
 * <i> The content of each inner include/exclude {@link ResourceMatcher} must be
 * validated before calling {@link #findResources()}. </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Resources extends ResourceMatcher {

	/**
	 * Nested Element, which specifies the resources to include.
	 */
	public static final String INCLUDE_NE = "include";

	/**
	 * Nested Element, which specifies the resources to exclude.
	 */
	public static final String EXCLUDE_NE = "exclude";

	private List<ResourceMatcher> _includes;
	private List<ResourceMatcher> _excludes;

	public Resources() {
		super();
		setIncludes(new ArrayList<ResourceMatcher>());
		setExcludes(new ArrayList<ResourceMatcher>());
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("local-basedir:");
		str.append(getLocalBaseDir());
		str.append(", match:");
		str.append(getMatch());
		str.append(", remote-basedir:");
		str.append(getRemoteBaseDir());
		str.append(", file-modifiers:");
		str.append(getFileModifiers());
		str.append(", dir-modifiers:");
		str.append(getDirModifiers());
		str.append(", group:");
		str.append(getGroup());
		str.append(", link-option:");
		str.append(getLinkOption());
		str.append(", transfer-behavior:");
		str.append(getTransferBehavior());
		str.append(", is-template:");
		str.append(getTemplate());
		str.append(", includes:");
		str.append(getIncludes());
		str.append(", excludes:");
		str.append(getExcludes());
		str.append(" }");
		return str.toString();
	}

	@Override
	public List<LocalResource> findResources() throws IOException {
		List<LocalResource> res = super.findResources();
		for (ResourceMatcher r : getIncludes()) {
			updateMatchingLocalResources(res, r);
		}
		for (ResourceMatcher r : getExcludes()) {
			excludeMatchingLocalResources(res, r);
		}
		return res;
	}

	private static void updateMatchingLocalResources(
			List<LocalResource> localResources, ResourceMatcher resourceMatcher) {
		String path = Paths.get(
				resourceMatcher.getLocalBaseDir().getAbsolutePath())
				.normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (LocalResource r : localResources) {
			if (matcher.matches(r.getPath())) {
				r.setResourceMatcher(resourceMatcher);
			}
		}
	}

	private static List<LocalResource> excludeMatchingLocalResources(
			List<LocalResource> localResources, ResourceMatcher resourceMatcher) {
		List<LocalResource> matching = new ArrayList<LocalResource>();
		String path = Paths.get(
				resourceMatcher.getLocalBaseDir().getAbsolutePath())
				.normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (LocalResource r : localResources) {
			if (matcher.matches(r.getPath())) {
				matching.add(r);
			}
		}
		localResources.removeAll(matching);
		return localResources;
	}

	@NestedElement(name = INCLUDE_NE, type = Type.CREATE)
	public ResourceMatcher addInclude() {
		ResourceMatcher include = new ResourceMatcher(this);
		getIncludes().add(include);
		return include;
	}

	@NestedElement(name = EXCLUDE_NE, type = Type.CREATE)
	public ResourceMatcher createExclude() {
		ResourceMatcher exclude = new ResourceMatcher(this);
		getExcludes().add(exclude);
		return exclude;
	}

	public List<ResourceMatcher> getIncludes() {
		return _includes;
	}

	public List<ResourceMatcher> setIncludes(List<ResourceMatcher> aIncludes) {
		if (aIncludes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourceMatcher.class.getCanonicalName()
					+ "> (an Inclusion List).");
		}
		List<ResourceMatcher> previous = getIncludes();
		_includes = aIncludes;
		return previous;
	}

	public List<ResourceMatcher> getExcludes() {
		return _excludes;
	}

	public List<ResourceMatcher> setExcludes(List<ResourceMatcher> aExcludes) {
		if (aExcludes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourceMatcher.class.getCanonicalName()
					+ "> (an Exclusion List).");
		}
		List<ResourceMatcher> previous = getExcludes();
		_excludes = aExcludes;
		return previous;
	}

}