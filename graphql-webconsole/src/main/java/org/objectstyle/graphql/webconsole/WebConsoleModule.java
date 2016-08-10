package org.objectstyle.graphql.webconsole;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.jetty.JettyModule;

public class WebConsoleModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {
        JettyModule.contributeStaticServlet(binder, "default", "/graphiql/*");

        BQCoreModule.contributeProperties(binder)
                .addBinding("bq.jetty.staticResourceBase")
                .toInstance(WebConsoleModule.class.getResource("").getPath());
	}
}
