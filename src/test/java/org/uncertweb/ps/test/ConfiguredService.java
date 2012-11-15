package org.uncertweb.ps.test;

import java.nio.file.Path;

import org.junit.rules.TemporaryFolder;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.ps.test.process.BufferPolygonProcess;
import org.uncertweb.ps.test.process.HashProcess;
import org.uncertweb.ps.test.process.SumProcess;

/**
 * Resource to spoof a configured service.
 * 
 * @author Richard Jones
 *
 */
public class ConfiguredService extends TemporaryFolder {
	
	private String baseURL;
	private Path storageRoot;
	
	public ConfiguredService() {
		super();
	}
	
	@Override
	public void before() throws Throwable {
		super.before();
		
		// - sets up process repo with extra classes
		ProcessRepository repo = ProcessRepository.getInstance();
		repo.addProcess(new HashProcess());
		repo.addProcess(new SumProcess());
		repo.addProcess(new BufferPolygonProcess());
		
		// - creates temp folder		
		storageRoot = this.newFolder().toPath();
		
		// - inits file storage for temp folder
		Config config = Config.getInstance();
		config.setStorageProperty("baseFolder", storageRoot.toString());
		
		// - sets server host:port in config
		baseURL = "http://localhost:9090/ps";
		config.setServerProperty("baseURL", baseURL);
	}
	
	@Override
	public void after() {
		super.after();
	}
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public Path getStorageRoot() {
		return storageRoot;
	}
	
}
