package org.objectstyle.graphql.bootique;

import java.util.function.Consumer;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.test.junit.JettyTestFactory;
import io.bootique.Bootique;

import org.apache.cayenne.configuration.server.ServerRuntime;


import org.objectstyle.graphql.test.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLApp extends JettyTestFactory {
    private TestFactory testFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLApp.class);

    protected GraphQLApp() {

    }

    @Override
    protected void before() {
        super.before();
        testFactory = new TestFactory();

        Consumer<Bootique> configurator = bq -> {
            bq.module(JerseyModule.class);
            bq.module(GraphQLModule.class);
            bq.module(b -> {
                        b.bind(ServerRuntime.class).toInstance(testFactory.getServerRuntime());
                    }
            );
        };

        newRuntime().configurator(configurator)
                .startServer("--config=src/test/resources/graphql-test.yml");
    }

    protected Response postGraphQL(String graphQL) {
        String json = "{\"query\":\"" + graphQL + "\"}";
        LOGGER.info(json);
        Entity<String> e = Entity.entity(json, MediaType.APPLICATION_JSON);
        return ClientBuilder.newClient().target("http://localhost:8081/").request().post(e);
    }
}
