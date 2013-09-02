package com.wat.melody.core.xpathfunctions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import com.wat.melody.api.Melody;

/**
 * <p>
 * XPath custom function, which create a new temporary file name.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class NewTmpFile implements XPathFunction {

	public static final String NAME = "newTmpFile";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		try {
			Path wfp = Paths.get(Melody.getContext().getProcessorManager()
					.getWorkingFolderPath());
			Files.createDirectories(wfp);
			return Files.createTempFile(wfp, null, null).getFileName();
		} catch (IOException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}