package com.wat.melody.xpathextensions;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import com.wat.melody.common.utils.Doc;

public abstract class CustomXPathFunctions {

	public static final String NAMESPACE = "melody";
	public static final String NAMESPACE_URI = "http://localhost/" + NAMESPACE;

	/**
	 * The 'herit' XML attribute of an XML Element in the Resources Descriptor
	 */
	public static final String HERIT_ATTR = "herit";

	public static void load() {

		Doc.getXPath().setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String s) {
				if (s.equals(NAMESPACE)) {
					return NAMESPACE_URI;
				}
				return null;
			}

			public String getPrefix(String s) {
				return null;
			}

			@SuppressWarnings("rawtypes")
			public Iterator getPrefixes(String s) {
				return null;
			}
		});

		Doc.getXPath().setXPathFunctionResolver(
				new MelodyXPathFunctionResolver());
	}

}
