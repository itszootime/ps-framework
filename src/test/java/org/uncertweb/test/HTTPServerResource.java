package org.uncertweb.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServerResource extends ExternalResource {

	private final InetSocketAddress address;
	private HttpServer server;
	
	public HTTPServerResource(int port) {
		address = new InetSocketAddress(port);
	}
	
	@Override
	protected void before() throws Throwable {
		super.before();
		server = HttpServer.create(address, 0);
		server.start();
	}
	
	public void addFileHandler(final String filename) {
		server.createContext("/" + filename, new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				String response = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(filename));
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
		        exchange.getResponseBody().write(response.getBytes());
		        exchange.close();
			}
		});
	}
	
	@Override
	protected void after() {
		server.stop(0);
		super.after();
	}
	
}
