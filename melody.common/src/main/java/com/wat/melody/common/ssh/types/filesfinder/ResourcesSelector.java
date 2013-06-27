package com.wat.melody.common.ssh.types.filesfinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.ssh.impl.filefinder.LocalResourcesUpdaterExcludes;
import com.wat.melody.common.ssh.impl.filefinder.LocalResourcesUpdaterIncludes;

/**
 * <p>
 * A {@link ResourcesSelector} describes which and how files and directories
 * should be transfered.
 * </p>
 * 
 * <p>
 * A {@link ResourcesSelector} is an {@link ITask} nested element. It can
 * contains nested include/exclude elements, which can override how files and
 * directories should be transfered.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourcesSelector extends ResourceSpecification {

	/**
	 * Attribute, which specifies the local directory.
	 */
	public static final String LOCAL_BASEDIR_ATTR = "local-basedir";

	/**
	 * Attribute, which specifies the remote directory.
	 */
	public static final String REMOTE_BASEDIR_ATTR = "remote-basedir";

	/**
	 * Nested Element, which specifies the resources to include.
	 */
	public static final String INCLUDE_NE = "include";

	/**
	 * Nested Element, which specifies the resources to exclude.
	 */
	public static final String EXCLUDE_NE = "exclude";

	// Mandatory with no default value
	private File _localBaseDir = null;
	// Mandatory with a default value
	private String _remoteBaseDir = ".";
	private List<ResourcesUpdater> _resourcesUpdaters = new ArrayList<ResourcesUpdater>();

	public ResourcesSelector() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("match:");
		str.append(getMatch());
		str.append(", local-basedir:");
		str.append(getLocalBaseDir());
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
		str.append(", includes-excludes:");
		str.append(getResourcesUpdaters());
		str.append(" }");
		return str.toString();
	}

	@NestedElement(name = INCLUDE_NE, type = Type.CREATE)
	public ResourceSpecification addInclude() {
		LocalResourcesUpdaterIncludes include = new LocalResourcesUpdaterIncludes(
				this);
		getResourcesUpdaters().add(include);
		return include;
	}

	@NestedElement(name = EXCLUDE_NE, type = Type.CREATE)
	public ResourceSpecification createExclude() {
		LocalResourcesUpdaterExcludes exclude = new LocalResourcesUpdaterExcludes(
				this);
		getResourcesUpdaters().add(exclude);
		return exclude;
	}

	public File getLocalBaseDir() {
		return _localBaseDir;
	}

	@Attribute(name = LOCAL_BASEDIR_ATTR)
	public File setLocalBaseDir(File basedir) {
		if (basedir == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Mus be a valid " + File.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		File previous = getLocalBaseDir();
		_localBaseDir = basedir;
		return previous;
	}

	public String getRemoteBaseDir() {
		return _remoteBaseDir;
	}

	@Attribute(name = REMOTE_BASEDIR_ATTR)
	public String setRemoteBaseDir(String destination) {
		if (destination == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Directory Path).");
		}
		String previous = getRemoteBaseDir();
		_remoteBaseDir = destination;
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