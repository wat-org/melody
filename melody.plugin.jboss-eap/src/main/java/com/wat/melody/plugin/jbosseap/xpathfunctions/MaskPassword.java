package com.wat.melody.plugin.jbosseap.xpathfunctions;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.jboss.security.plugins.PBEUtils;
import org.picketbox.plugins.vault.PicketBoxSecurityVault;

/**
 * <p>
 * Given a 'salt', an 'iteration count' and a 'clear password', this XPath
 * custom function will return the corresponding encrypted password.
 * </p>
 * 
 * <p>
 * The encryption algorithm is the same as the one used in the JBoss Vault.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class MaskPassword implements XPathFunction {

	public static final String NAME = "maskPassword";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		Object arg0 = list.get(0);
		Object arg1 = list.get(1);
		Object arg2 = list.get(2);
		if (arg0 == null || !(arg0 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as first argument.");
		}
		if (arg1 == null || !(arg1 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as second argument.");
		}
		if (arg2 == null || !(arg2 instanceof String)) {
			throw new XPathFunctionException("null: Not accepted. " + NAME
					+ "() expects a non-null String as third argument.");
		}
		try {
			byte[] salt = ((String) arg0).substring(0, 8).getBytes();
			int count = Integer.parseInt((String) arg1);
			char[] password = "somearbitrarycrazystringthatdoesnotmatter"
					.toCharArray();
			byte[] passwordToEncode = ((String) arg2).getBytes("UTF-8");

			PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
			PBEKeySpec keySpec = new PBEKeySpec(password);
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance("PBEwithMD5andDES");
			SecretKey cipherKey = factory.generateSecret(keySpec);

			String encodedPassword = PBEUtils.encode64(passwordToEncode,
					"PBEwithMD5andDES", cipherKey, cipherSpec);

			return PicketBoxSecurityVault.PASS_MASK_PREFIX + encodedPassword;
		} catch (Exception Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}