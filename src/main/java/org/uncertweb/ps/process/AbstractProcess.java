package org.uncertweb.ps.process;

import java.util.List;

import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.ProcessOutputs;

public abstract class AbstractProcess {

	public String getIdentifier() {
		return this.getClass().getSimpleName();
	}
	
	public abstract String getDetail();
	
	public abstract List<String> getInputIdentifiers();
	public abstract List<String> getOutputIdentifiers();
	
	public abstract DataDescription getInputDataDescription(String identifier);
	public abstract DataDescription getOutputDataDescription(String identifier);
	
	public abstract List<Metadata> getInputMetadata(String identifier);
	public abstract List<Metadata> getOutputMetadata(String identifier);
	
	public abstract ProcessOutputs run(ProcessInputs inputs) throws ProcessException;
	
}
