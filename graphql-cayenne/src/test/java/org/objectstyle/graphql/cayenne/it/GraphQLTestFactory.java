package org.objectstyle.graphql.cayenne.it;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.query.SQLTemplate;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.objectstyle.graphql.cayenne.orm.DefaultSchemaTranslator;
import org.objectstyle.graphql.cayenne.orm.SchemaTranslator;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class GraphQLTestFactory {
    private static GraphQL graphQL;

    private static ServerRuntime runtime;
    private static ObjectContext context;


    @BeforeClass
    public static void setUpBeforeClass() {
        removeDerby();

        GraphQLTestFactory.runtime = ServerRuntimeBuilder.builder().addConfig("cayenne-tests.xml")
                .url("jdbc:derby:target/derby;create=true").jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver")
                .addModule(cayenneExtensions()).build();

        context = runtime.newContext();

        graphQL = createGraphQL(createSchemaTranslator());
    }

    @AfterClass
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;

        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // the exception is actually expected on shutdown... go figure...
        }
    }

    private static org.apache.cayenne.di.Module cayenneExtensions() {
        return (b) -> b.bind(SchemaUpdateStrategy.class).to(CreateIfNoSchemaStrategy.class);
    }

    private static GraphQL createGraphQL(SchemaTranslator translator) {
        GraphQLSchema schema = translator.toGraphQL(runtime.getChannel().getEntityResolver());
        return new GraphQL(schema);
    }

    private static SchemaTranslator createSchemaTranslator() {
        return new DefaultSchemaTranslator(context);
    }

    static void insert(String table, String columns, String values) {
        String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
        context.performGenericQuery(new SQLTemplate(table, insertSql));
    }

    String post_graphql_request(String request) {
        System.out.println(request);
        Object r = graphQL.execute(request).getData();
        return r == null ? "" : r.toString();
    }

    private static void removeDerby() {

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
