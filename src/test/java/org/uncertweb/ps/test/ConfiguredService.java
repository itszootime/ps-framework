package org.uncertweb.ps.test;

import java.nio.file.Path;

import org.junit.rules.ExternalResource;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.ps.test.process.BufferPolygonProcess;
import org.uncertweb.ps.test.process.HashProcess;
import org.uncertweb.ps.test.process.SumProcess;

public class ConfiguredService extends ExternalResource {
	
	// TODO: resource which spoofs configured service:
	// - sets up process repo with extra classes
	// - creates temp folder
	// - inits file storage for temp folder
	// - sets server host:port in config
	
	private String baseURL;
	private Path storageRoot;
	
	public static void setupProcessRepository() {
		// add processes
		ProcessRepository repo = ProcessRepository.getInstance();
		repo.addProcess(new HashProcess());
		repo.addProcess(new SumProcess());
		repo.addProcess(new BufferPolygonProcess());
	}
	
	public static void setupServerConfig() {
		Config cfg = Config.getInstance();
		cfg.setServerProperty("baseURL", BASE_URL);
	}
	
	public static void setupStorageConfig(Path base) {
		Config cfg = Config.getInstance();
		cfg.setStorageProperty("baseFolder", base.toString());
	}
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public Path getStorageRoot() {
		return storageRoot;
	}
	
}
