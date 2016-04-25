package org.objectstyle.graphql.webconsole;

import java.net.URL;
import java.util.Objects;

public class WebConsoleServlet extends ResourceServlet {

	private static final long serialVersionUID = 7972600981940595010L;

	static URL baseUrl() {
		URL baseUrl = WebConsoleServlet.class.getResource("");
		return Objects.requireNonNull(baseUrl);
	}

	public WebConsoleServlet() {
		super(baseUrl());
	}
}
