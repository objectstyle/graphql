package org.objectstyle.graphql.bootique;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.nhl.bootique.jersey.JerseyModule;

import com.nhl.bootique.jetty.test.junit.JettyTestFactory;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;

import com.nhl.bootique.Bootique;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.commons.io.FileUtils;

public class GraphQLApp extends JettyTestFactory {
    private ServerRuntime serverRuntime;
    private static ObjectContext context;

    protected GraphQLApp() {

    }

    @Override
    protected void before() {
        super.before();

        removeDerby();

        serverRuntime = ServerRuntimeBuilder.builder().addConfig("cayenne-tests.xml")
                .url("jdbc:derby:target/derby;create=true").jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver")
                .addModule(cayenneExtensions()).build();

        Consumer<Bootique> configurator = bq -> {
            bq.module(JerseyModule.class);
            bq.module(GraphQLModule.class);
            bq.module(b -> {
                        b.bind(ServerRuntime.class).toInstance(serverRuntime);
                    }
            );
        };

        newRuntime().configurator(configurator)
                .startServer("--config=src/test/resources/graphql-test.yml");

        context = serverRuntime.newContext();
    }

    private static org.apache.cayenne.di.Module cayenneExtensions() {
        return (b) -> b.bind(SchemaUpdateStrategy.class).to(CreateIfNoSchemaStrategy.class);
    }

    protected Response postGraphQL(String graphQL) {
        String json = "{\"query\":\"" + graphQL + "\"}";
        System.out.println(json);
        Entity<String> e = Entity.entity(json, MediaType.APPLICATION_JSON);
        return ClientBuilder.newClient().target("http://localhost:8081/").request().post(e);
    }

    public ServerRuntime getServerRuntime() {
        return serverRuntime;
    }

    public static void insert(String table, String columns, String values) {
        String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
        context.performGenericQuery(new SQLTemplate(table, insertSql));
    }

    protected static void removeDerby() {

        File derbyDir = new File("target/derby");
        if (derbyDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(derbyDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
