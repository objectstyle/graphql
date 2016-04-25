package org.objectstyle.graphql.webconsole;

import com.google.inject.Binder;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.jetty.MappedServlet;

import java.util.Arrays;
import java.util.HashSet;

public class WebConsoleModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {
        MappedServlet mappedServlet = new MappedServlet(new WebConsoleServlet(), new HashSet<>(Arrays.asList("/graphiql/*")));
		JettyModule.contributeServlets(binder).addBinding().toInstance(mappedServlet);
	}
}
