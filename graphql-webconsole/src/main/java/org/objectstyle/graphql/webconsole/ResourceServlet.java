package org.objectstyle.graphql.webconsole;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: this should either be in Bootique-Jetty or we should find some 
// existing Jetty API that does the same thing...
public abstract class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1499214608341399558L;

	private URL resourceBase;

	public ResourceServlet(URL resourceBase) {
		this.resourceBase = resourceBase;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// TODO: mapping to index.html, etc...
		// TODO: response codes for 404, etc.

		String path = req.getPathInfo();
		if (path == null) {
			path = "";
		}
		// all URLs must be relative, so strip leading slash...
		else if (path.startsWith("/")) {
			path = path.substring(1);
		}

		URL resourceURL = new URL(resourceBase, path);

		try (InputStream in = resourceURL.openStream()) {

			byte[] buffer = new byte[8 * 1024];

			ServletOutputStream out = resp.getOutputStream();

			int i = 0;
			while ((i = in.read(buffer)) > 0) {
				out.write(buffer, 0, i);
			}

			out.flush();
		}
	}
}
