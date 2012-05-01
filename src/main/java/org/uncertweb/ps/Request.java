package org.uncertweb.ps;

import java.util.ArrayList;
import java.util.List;

import org.uncertweb.ps.data.ProcessInputs;

public class Request {

	private String processIdentifier;
	private ProcessInputs inputs;
	private List<RequestedOutput> requestedOutputs;
	
	public Request(String processIdentifier) {
		this.processIdentifier = processIdentifier;
		this.inputs = new ProcessInputs();
		this.requestedOutputs = new ArrayList<RequestedOutput>();
	}

	public String getProcessIdentifier() {
		return processIdentifier;
	}

	public ProcessInputs getInputs() {
		return inputs;
	}
	
	public List<RequestedOutput> getRequestedOutputs() {
		return requestedOutputs;
	}
	
}
