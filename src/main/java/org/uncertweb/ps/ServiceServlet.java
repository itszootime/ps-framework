package org.uncertweb.ps;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.uncertweb.ps.handler.json.JSONDescriptionHelper;
import org.uncertweb.ps.handler.json.JSONHandler;
import org.uncertweb.ps.handler.soap.SOAPHandler;
import org.uncertweb.ps.handler.soap.XMLSchemaGenerator;
import org.uncertweb.ps.handler.soap.WSDLGenerator;
import org.uncertweb.util.Stopwatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class ServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger(ServiceServlet.class);
	
	// FIXME: servlet should validate all this is done:
	// - file storage can be initialised
	// - host:port set in config
	
	/**
	 * GET method should:
	 * 
	 * - handle requests ?wsdl
	 * - display info page: list of processes, link to wsdl, other info?...
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
		// get writer
		PrintWriter writer = servletResponse.getWriter();

		// handle wsdl requests
		String queryString = servletRequest.getQueryString();
		if (queryString != null) {
			if (servletRequest.getQueryString().equals("wsdl")) {
				// generate wsdl
				Document document = new WSDLGenerator().generateDocument("http://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletRequest.getContextPath() + "/service");

				// output document
				servletResponse.setContentType("text/xml");
				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
				outputter.output(document, writer);
			}
			else if (queryString.equals("schema")) {
				// generate schema
				Document document = new XMLSchemaGenerator().generateDocument();

				// output document
				servletResponse.setContentType("text/xml");
				XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
				outputter.output(document, writer);
			}
			else if (queryString.equals("jsondesc")) {
				// generate a json description
				JsonElement element = JSONDescriptionHelper.generateJsonDescription();
				
				// output document
				servletResponse.setContentType("application/json");
				Gson gson = new GsonBuilder().create();
				gson.toJson(element, writer);
			}
		}
		else {
			// display info page
			servletResponse.setContentType("text/html");
			writer.write("<a href=\"?wsdl\">wsdl</a>");
		}

		// all done!
		writer.close();
	}

	/**
	 * 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
		logger.info("Incoming request from " + servletRequest.getRemoteHost() + ".");
		logger.debug("Request content type is " + servletRequest.getHeader("Content-Type"));
		Stopwatch timer = new Stopwatch();
		
		// get context details
		String basePath = this.getServletContext().getRealPath("/");
		String baseURL = "http://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletRequest.getContextPath();
		
		// store in config
		Config config = Config.getInstance();
		config.setServerProperty("basePath", basePath);
		config.setServerProperty("baseURL", baseURL);
		
		// get encoding from request url
		String pathInfo = servletRequest.getPathInfo();
		if (pathInfo != null) {
			if (pathInfo.equals("/soap")) {
				// soap 1.1
				servletResponse.setContentType("text/xml");
				servletResponse.addHeader("SOAPAction", "http://www.uncertweb.org/ProcessingService");
				SOAPHandler requestHandler = new SOAPHandler();
				requestHandler.handleRequest(servletRequest.getInputStream(), servletResponse.getOutputStream());
			}
			else if (pathInfo.equals("/json")) {
				servletResponse.setContentType("application/json");
				JSONHandler requestHandler = new JSONHandler();
				requestHandler.handleRequest(servletRequest.getReader(), servletResponse.getWriter());
			}
		}
		
		logger.info("Handled request in " + timer.getElapsedTime() + ".");
	}

}
