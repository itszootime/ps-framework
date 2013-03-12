package org.uncertweb.test;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.rules.ExternalResource;

public class HTTPFileServer extends ExternalResource {

	private Server server;
	
	public HTTPFileServer(int port) {
		server = new Server(port);
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		
		String base = this.getClass().getClassLoader().getResource(".").getPath();
		resourceHandler.setResourceBase(base);
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler() });
        server.setHandler(handlers);
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		server.start();
	}

	@Override
	protected void after() {
		try {
			server.stop();
		}
		catch (Exception e) {
			System.err.println("Couldn't shut down HTTP server");
			e.printStackTrace();
		}
		super.after();
	}

}
