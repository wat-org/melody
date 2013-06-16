package com.wat.melody.common.ssh.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.ssh.types.exception.ResourceException;

/**
 * <p>
 * A {@link Resources} is especially design to find files and directories, based
 * on multiple {@link ResourceMatcher} include/exclude conditions.
 * </p>
 * 
 * <p>
 * <i> The content of each inner include/exclude {@link ResourceMatcher} must be
 * validated before calling {@link #findResources()}. </i>
 * </p>
 * 
 * <p>
 * <i> A {@link Resources} is an {@link ITask}, which can contains
 * include/exclude {@link ResourceMatcher}. It is itself a
 * {@link ResourceMatcher}. All attributes defined in it directly are passed as
 * default values for the inner include and exclude {@link ResourceMatcher}. <BR/>
 * </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Resources extends ResourceMatcher {

	/**
	 * The 'include' XML Nested Element
	 */
	public static final String INCLUDE_NE = "include";

	/**
	 * The 'exclude' XML Nested Element
	 */
	public static final String EXCLUDE_NE = "exclude";

	private List<ResourceMatcher> _includes;
	private List<ResourceMatcher> _excludes;

	public Resources() {
		super();
		initIncludes();
		initExcludes();
	}

	private void initIncludes() {
		_includes = new ArrayList<ResourceMatcher>();
	}

	private void initExcludes() {
		_excludes = new ArrayList<ResourceMatcher>();
	}

	@Override
	public String toString() {
		return "{ " + "basedir:" + getLocalBaseDir() + ", match:" + getMatch()
				+ ", destination:" + getRemoteBaseDir() + ", fileModifiers:"
				+ getFileModifiers() + ", dirModifiers:" + getDirModifiers()
				+ ", group:" + getGroup() + ", includes:" + getIncludes()
				+ ", excludes:" + getExcludes() + " }";
	}

	@Override
	public List<SimpleResource> findResources() throws IOException {
		List<SimpleResource> res = super.findResources();
		List<SimpleResource> tmp;
		for (ResourceMatcher r : getIncludes()) {
			tmp = r.findResources();
			res.removeAll(tmp); // remove duplicated element
			res.addAll(tmp);
		}
		for (ResourceMatcher r : getExcludes()) {
			tmp = r.findResources();
			res.removeAll(tmp);
		}
		return res;
	}

	@NestedElement(name = INCLUDE_NE, type = Type.CREATE)
	public ResourceMatcher addInclude() throws ResourceException {
		ResourceMatcher include = new ResourceMatcher(this);
		getIncludes().add(include);
		return include;
	}

	@NestedElement(name = EXCLUDE_NE, type = Type.CREATE)
	public ResourceMatcher createExclude() throws ResourceException {
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
					+ "Must be a valid List<ResourcesBase> (a Resource "
					+ "Inclusion List).");
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
					+ "Must be a valid List<ResourcesBase> (a Resource "
					+ "Exclusion List).");
		}
		List<ResourceMatcher> previous = getExcludes();
		_excludes = aExcludes;
		return previous;
	}

}