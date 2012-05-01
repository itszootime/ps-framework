package org.uncertweb.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DataServlet
 */
public class DataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("id") != null) {
			// TODO: set content type
			// TODO: store details in some sort of database?
			
			// get file and setup streams
			File file = new File(this.getServletContext().getRealPath("/WEB-INF") + System.getProperty("file.separator") + "out_" + request.getParameter("id"));
			FileInputStream in = new FileInputStream(file);
			OutputStream out = response.getOutputStream();

			// copy in to out
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			do {
				bytesRead = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, bytesRead);
			} while (bytesRead == buffer.length);

			// close streams
			in.close();
			out.close();
		}
	}

}
