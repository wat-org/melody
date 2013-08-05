package com.wat.melody.common.transfer.resources;

import java.nio.file.Path;
import java.util.List;

import com.wat.melody.common.transfer.Transferable;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ResourcesUpdater {

	public boolean isMatching(Path path);

	public void update(List<Transferable> list, Transferable t);

}