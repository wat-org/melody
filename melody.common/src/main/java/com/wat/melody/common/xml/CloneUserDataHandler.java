package com.wat.melody.common.xml;

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class CloneUserDataHandler implements UserDataHandler {

	@Override
	public void handle(short op, String key, Object data, Node src, Node dst) {
		if (dst == null) {
			return;
		}
		dst.setUserData(key, data, this);
	}

}