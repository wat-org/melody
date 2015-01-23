package com.wat.melody.common.xpath;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XPathErrorListener implements ErrorListener {

	private static Logger log = LoggerFactory
			.getLogger(XPathErrorListener.class);
	private static Logger ex = LoggerFactory.getLogger("exception."
			+ XPathErrorListener.class);

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
		MelodyException mex = new MelodyException(exception);
		log.warn(mex.getUserFriendlyStackTrace());
		// now the warn message is traced, we don't need to throw the exception

		// TODO : the message will be traced, but such message - in my opinion -
		// does not contains accurate information. We don't know which file,
		// node, expression contains the error. We must find a way to give more
		// precision!
	}

	@Override
	public void error(TransformerException exception)
			throws TransformerException {
		// since we throw the exception, really don't know if it is necessary to
		// trace the message
		MelodyException mex = new MelodyException(exception);
		log.error(mex.getUserFriendlyStackTrace());
		ex.error(mex.getFullStackTrace());
		throw exception;
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		// since we throw the exception, really don't know if it is necessary to
		// trace the message
		MelodyException mex = new MelodyException(exception);
		log.error(mex.getUserFriendlyStackTrace());
		ex.error(mex.getFullStackTrace());
		throw exception;
	}

}