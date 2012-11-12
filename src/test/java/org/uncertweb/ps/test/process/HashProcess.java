package org.uncertweb.ps.test.process;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.SingleOutput;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;


public class HashProcess extends AbstractProcess {

	public List<String> getInputIdentifiers() {
		return Arrays.asList(new String[] { "String" });
	}

	public List<String> getOutputIdentifiers() {
		return Arrays.asList(new String[] { "MD5", "SHA1" });
	}

	public DataDescription getInputDataDescription(String identifier) {
		return new DataDescription(String.class);
	}

	public DataDescription getOutputDataDescription(String identifier) {
		return new DataDescription(String.class);
	}

	public ProcessOutputs run(ProcessInputs inputs) throws ProcessException {
		// get a and b
		String string = inputs.get("String").getAsSingleInput().getObjectAs(String.class);
		
		// hash
		String md5;
		String sha1;
		try {
			byte[] messageBytes = string.getBytes("UTF-8");
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
			md5 = md5Digest.digest(messageBytes).toString();
			sha1 = sha1Digest.digest(messageBytes).toString();
		}
		catch (UnsupportedEncodingException e) {
			throw new ProcessException("Couldn't get bytes from string.", e);
		}
		catch (NoSuchAlgorithmException e) {
			throw new ProcessException("Couldn't find hashing algorithm.", e);
		}

		// return
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("MD5", md5));
		outputs.add(new SingleOutput("SHA1", sha1));
		return outputs;
	}

	@Override
	public List<Metadata> getInputMetadata(String arg0) {
		Metadata description = new Metadata("description", "The string to hash");
		return Arrays.asList(new Metadata[] { description });
	}

	@Override
	public List<Metadata> getOutputMetadata(String arg0) {
		Metadata description;
		if (arg0.equals("MD5")) {
			description = new Metadata("description", "The MD5 hashed string");
		}
		else {
			description = new Metadata("description", "The SHA-1 hashed string");
		}
		return Arrays.asList(new Metadata[] { description });
	}

	@Override
	public List<Metadata> getMetadata() {
		Metadata description = new Metadata("description", "Hash a string");
		return Arrays.asList(new Metadata[] { description });
	}
	
}
