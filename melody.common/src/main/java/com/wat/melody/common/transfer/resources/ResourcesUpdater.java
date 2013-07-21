package com.wat.melody.common.transfer.resources;

import java.util.List;

import com.wat.melody.common.transfer.Transferable;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ResourcesUpdater {

	public void update(List<Transferable> root);

}