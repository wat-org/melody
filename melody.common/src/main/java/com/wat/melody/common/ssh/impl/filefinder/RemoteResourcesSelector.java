package com.wat.melody.common.ssh.impl.filefinder;

import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesUpdater;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RemoteResourcesSelector extends ResourcesSelector {

	public ResourcesUpdater newResourcesUpdaterIncludes(ResourcesSelector rs) {
		return new RemoteResourcesUpdaterIncludes(rs);
	}

	public ResourcesUpdater newResourcesUpdaterExcludes(ResourcesSelector rs) {
		return new RemoteResourcesUpdaterExcludes(rs);
	}

}