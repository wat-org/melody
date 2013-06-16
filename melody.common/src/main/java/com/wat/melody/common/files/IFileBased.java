package com.wat.melody.common.files;

/**
 * <p>
 * Any object which implements {@link IFileBased} must have a constructor with
 * one {@link String} argument. When such object will be created using this
 * constructor, the task factory will relativize the given path to the sequence
 * descriptor basedir.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IFileBased {

}