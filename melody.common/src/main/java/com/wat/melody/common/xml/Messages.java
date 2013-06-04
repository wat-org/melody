package com.wat.melody.common.xml;

import org.eclipse.osgi.util.NLS;

import com.wat.melody.common.systool.SysTool;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.common.xml.messages";

	public static String NoSuchDUNIDEx_UNFOUND;

	public static String DUNIDEx_EMPTY;
	public static String DUNIDEx_INVALID;

	public static String DUNIDDocEx_FOUND_DUNID;
	public static String DUNIDDocEx_FOUND_DUNID_RESUME;
	public static String DUNIDDocEx_FORBIDDEN_OP;
	public static String DUNIDDocEx_UNEXPECTED_ERR;
	public static String DUNIDDocEx_DUNID_ADD;
	public static String DUNIDDocEx_DUNID_DEL;
	public static String DUNIDDocEx_DUNID_MOD;
	public static String DUNIDDocMsg_NODE_INSERTED;
	public static String DUNIDDocMsg_NODE_REMOVED;
	public static String DUNIDDocMsg_NODE_TEXT_CHANGED;
	public static String DUNIDDocMsg_ATTRIBUTE_INSERTED;
	public static String DUNIDDocMsg_ATTRIBUTE_REMOVED;
	public static String DUNIDDocMsg_ATTRIBUTE_MODIFIED;

	public static String DocEx_INVALID_XML_SYNTAX_AT;
	public static String DocEx_INVALID_XML_SYNTAX;
	public static String DocEx_INVALID_XML_DATA;

	public static String FilteredDocEx_DUPLICATE;
	public static String FilteredDocEx_INCORRECT_XPATH;
	public static String FilteredDocEx_TOO_RSTRICTIVE;
	public static String FilteredDocEx_MUST_TARGET_ELEMENT;
	public static String HeritAttrEx_INVALID_XPATH;
	public static String HeritAttrEx_MATCH;
	public static String HeritAttrEx_MATCH_RESUME;
	public static String HeritAttrEx_NO_MATCH;
	public static String HeritAttrEx_DONT_MATCH_ELEMENT;
	public static String HeritAttrEx_CIRCULAR_REF;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String bind(String message, Object... bindings) {
		for (int i = 0; i < bindings.length; i++) {
			bindings[i] = bindings[i].toString().replaceAll(SysTool.NEW_LINE,
					SysTool.NEW_LINE + "  ");
		}
		return NLS.bind(message, bindings);
	}

	private Messages() {
	}

}