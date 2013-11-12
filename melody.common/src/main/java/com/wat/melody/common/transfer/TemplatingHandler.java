package com.wat.melody.common.transfer;

import java.io.File;
import java.nio.file.Path;

import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface TemplatingHandler {

	/**
	 * <p>
	 * Expand the content of the template file into the destination file.
	 * </p>
	 * 
	 * @param template
	 *            is the {@link Path} of the {@link File} to expand.
	 * @param destination
	 *            is the {@link Path} of the {@link File} where the expansion
	 *            result will be stored. Can be <tt>null</tt>. If <tt>null</tt>,
	 *            {@link Path} of the resulting {@link File} will be
	 *            automatically computed by this method.
	 * 
	 * @return the {@link Path} of the resulting expanded {@link File}.
	 * 
	 * @throws TemplatingException
	 *             <ul>
	 *             <li>if an expression cannot be expanded because it is not a
	 *             valid expression (ex: circular ref, invalid character, ...) ;
	 *             </li>
	 *             <li>if any of the given {@link Path} doesn't point to a valid
	 *             {@link File} ;</li>
	 *             <li>if an IO error occurred while reading/writing
	 *             {@link File}s which are pointed by the given {@link Path}s ;</li>
	 *             </ul>
	 * @throws IllegalArgumentException
	 *             if fileToExpand is <code>null</code>.
	 */
	public Path doTemplate(Path template, Path destination)
			throws TemplatingException;

}