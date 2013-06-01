package com.wat.melody.common.xml;

import org.w3c.dom.events.MutationEvent;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface DocListener {

	public void nodeInstered(MutationEvent evt) throws MelodyException;

	public void nodeRemoved(MutationEvent evt) throws MelodyException;

	public void nodeTextChanged(MutationEvent evt) throws MelodyException;

	public void attributeInserted(MutationEvent evt) throws MelodyException;

	public void attributeRemoved(MutationEvent evt) throws MelodyException;

	public void attributeModified(MutationEvent evt) throws MelodyException;

}