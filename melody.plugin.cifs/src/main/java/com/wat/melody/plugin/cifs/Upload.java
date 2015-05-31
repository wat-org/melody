package com.wat.melody.plugin.cifs;

import java.io.File;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.common.cifs.transfer.CifsUploaderMultiThread;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.plugin.cifs.common.Transfer;
import com.wat.melody.plugin.cifs.common.types.LocalResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "cifs") }),
		@Condition({ @Match(expression = "§[machine.os.name]§", value = "windows") }) })
public class Upload extends Transfer {

	/**
	 * Task's name
	 */
	public static final String UPLOAD = "upload";

	public Upload() {
		super();
	}

	@Override
	public void doTransfer(String location, String domain, String username,
			String password) throws TransferException, InterruptedException {
		new CifsUploaderMultiThread(location, domain, username, password,
				getResourcesSpecifications(), getMaxPar(), this,
				Melody.getThreadFactory()).doTransfer();
	}

	@Override
	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new LocalResourcesSpecification(basedir);
	}

}