package org.uncertweb.ps.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.uncertweb.ps.DataServlet;
import org.uncertweb.ps.ServiceServlet;

public class RunningService extends ConfiguredService {

	private Server server;
	
	public RunningService() {
		super();
	}
	
	@Override
	public void before() throws Throwable {
		super.before();
		
		// this one needs to launch a servlet container
		server = new Server(9090);
		
		// set context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/ps");
        server.setHandler(context);
 
        // add service and data servlets
        context.addServlet(new ServletHolder(new ServiceServlet()), "/service/*");
        context.addServlet(new ServletHolder(new DataServlet()), "/data");
 
        // start server!
        server.start();
	}
	
	@Override
	public void after() {
		try {
			server.stop();
		}
		catch (Exception e) {
			// ignore
		}
		super.after();
	}
	
}
