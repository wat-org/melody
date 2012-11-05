package com.wat.melody.core.internal;

import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.common.utils.FilteredDoc;
import com.wat.melody.common.utils.exception.IllegalFilterException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleResourcesDescriptor extends FilteredDoc {

	public void load(FilteredDoc doc) throws IllegalResourcesFilterException {
		try {
			super.load(doc);
		} catch (IllegalFilterException Ex) {
			throw new IllegalResourcesFilterException(Ex);
		}
	}

}
