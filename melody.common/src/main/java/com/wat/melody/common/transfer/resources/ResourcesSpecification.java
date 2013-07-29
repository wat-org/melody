package com.wat.melody.common.transfer.resources;

import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;

/**
 * <p>
 * A {@link ResourcesSpecification} describes which and how files and
 * directories should be transfered.
 * </p>
 * 
 * <p>
 * A {@link ResourcesSpecification} is an {@link ITask} nested element. It can
 * contains nested include/exclude elements, which can override how files and
 * directories should be transfered.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ResourcesSpecification extends ResourceSpecification {

	/**
	 * Attribute, which specifies the source directory.
	 */
	public static final String SRC_BASEDIR_ATTR = "src-basedir";

	/**
	 * Attribute, which specifies the destination directory.
	 */
	public static final String DEST_BASEDIR_ATTR = "dest-basedir";

	/**
	 * Nested Element, which specifies the resources to include.
	 */
	public static final String INCLUDE_NE = "include";

	/**
	 * Nested Element, which specifies the resources to exclude.
	 */
	public static final String EXCLUDE_NE = "exclude";

	private String _srcBaseDir;
	private String _destBaseDir;
	private List<ResourcesUpdater> _resourcesUpdaters = new ArrayList<ResourcesUpdater>();

	public ResourcesSpecification(String srcBaseDir, String destBaseDir) {
		super();
		setSrcBaseDir(srcBaseDir);
		setDestBaseDir(destBaseDir);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("match:");
		str.append(getMatch());
		str.append(", src-basedir:");
		str.append(getSrcBaseDir());
		str.append(", dest-basedir:");
		str.append(getDestBaseDir());
		str.append(", file-attributes:");
		str.append(getFileAttributesMap().values());
		str.append(", dir-attributes:");
		str.append(getDirAttributesMap().values());
		str.append(", link-option:");
		str.append(getLinkOption());
		str.append(", transfer-behavior:");
		str.append(getTransferBehavior());
		str.append(", is-template:");
		str.append(getTemplate());
		str.append(", includes-excludes:");
		str.append(getResourcesUpdaters());
		str.append(" }");
		return str.toString();
	}

	public ResourcesUpdater newResourcesUpdaterInclude(ResourcesSpecification rs) {
		return new ResourcesUpdaterIncludes(rs);
	}

	public ResourcesUpdater newResourcesUpdaterExclude(ResourcesSpecification rs) {
		return new ResourcesUpdaterExcludes(rs);
	}

	@NestedElement(name = INCLUDE_NE, type = Type.CREATE)
	public ResourcesUpdater createInclude() {
		ResourcesUpdater include = newResourcesUpdaterInclude(this);
		getResourcesUpdaters().add(include);
		return include;
	}

	@NestedElement(name = EXCLUDE_NE, type = Type.CREATE)
	public ResourcesUpdater createExclude() {
		ResourcesUpdater exclude = newResourcesUpdaterExclude(this);
		getResourcesUpdaters().add(exclude);
		return exclude;
	}

	public String getSrcBaseDir() {
		return _srcBaseDir;
	}

	@Attribute(name = SRC_BASEDIR_ATTR, mandatory = true)
	public String setSrcBaseDir(String dir) {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		String previous = getSrcBaseDir();
		_srcBaseDir = dir;
		return previous;
	}

	public String getDestBaseDir() {
		return _destBaseDir;
	}

	@Attribute(name = DEST_BASEDIR_ATTR)
	public String setDestBaseDir(String dir) {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		String previous = getDestBaseDir();
		_destBaseDir = dir;
		return previous;
	}

	public List<ResourcesUpdater> getResourcesUpdaters() {
		return _resourcesUpdaters;
	}

	public List<ResourcesUpdater> setResourcesUpdaters(
			List<ResourcesUpdater> rulist) {
		if (rulist == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourcesUpdater.class.getCanonicalName()
					+ "> (an Inclusion List).");
		}
		List<ResourcesUpdater> previous = getResourcesUpdaters();
		_resourcesUpdaters = rulist;
		return previous;
	}

}