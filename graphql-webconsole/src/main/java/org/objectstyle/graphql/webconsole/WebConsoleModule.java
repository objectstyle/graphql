package org.objectstyle.graphql.webconsole;

import com.google.inject.Binder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.jetty.JettyModule;

public class WebConsoleModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {
        JettyModule.contributeStaticServlet(binder, "default", "/graphiql/*");

        BQCoreModule.contributeProperties(binder)
                .addBinding("bq.jetty.staticResourceBase")
                .toInstance(WebConsoleModule.class.getResource("").getPath());
	}
}
