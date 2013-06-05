package com.wat.melody.common.xml;

import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface DocListener {

	public void elementInstered(MutationEvent evt) throws MelodyException;

	public void elementRemoved(MutationEvent evt) throws MelodyException;

	public void textLeafInserted(MutationEvent evt) throws MelodyException;

	public void textLeafRemoved(MutationEvent evt) throws MelodyException;

	public void textLeafModified(MutationEvent evt) throws MelodyException;

	public void attributeInserted(MutationEvent evt) throws MelodyException;

	public void attributeRemoved(MutationEvent evt) throws MelodyException;

	public void attributeModified(MutationEvent evt) throws MelodyException;

}