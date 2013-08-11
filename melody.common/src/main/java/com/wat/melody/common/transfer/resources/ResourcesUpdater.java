package com.wat.melody.common.transfer.resources;

import java.nio.file.Path;

import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.finder.TransferablesTree;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ResourcesUpdater {

	public boolean isMatching(Path path);

	public void update(TransferablesTree root, Transferable t);

}