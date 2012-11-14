package org.uncertweb.test;

import java.io.IOException;
import java.net.BindException;
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
		int attempts = 0;
		int max = 3;
		while (server == null && attempts < max) {
			attempts++;
			try {
				server = HttpServer.create(address, 0);
			}
			catch (BindException e) {
				if (attempts == max) {
					throw e;
				}
				else {
					Thread.sleep(500);
				}
			}
		}
		server.start();
	}

	public void addFileHandler(final String filename) {
		server.createContext("/" + filename, new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				byte[] bytes= IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(filename));
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bytes.length);
				exchange.getResponseBody().write(bytes);
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
