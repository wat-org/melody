package com.wat.melody.common.ssh.types.filesfinder;

import java.util.List;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ResourcesUpdater {

	public void update(List<? extends Resource> list);

}