package com.wat.melody.common.files;

import java.io.File;

/**
 * <p>
 * A {@link File}, which provides a public 1-string-argument constructor and
 * implements {@link IFileBased}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperFile extends File implements IFileBased {

	private static final long serialVersionUID = -447936295989914255L;

	public WrapperFile(String path) {
		super(path);
	}

}