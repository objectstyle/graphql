package org.objectstyle.graphql.cayenne.it;

import org.junit.AfterClass;

import org.objectstyle.graphql.cayenne.orm.DefaultSchemaTranslator;
import org.objectstyle.graphql.cayenne.orm.SchemaTranslator;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.objectstyle.graphql.test.TestFactory;

class GraphQLTestFactory {
    private static GraphQL graphQL;
    private static TestFactory testFactory = new TestFactory();

    GraphQLTestFactory() {
        graphQL = createGraphQL(createSchemaTranslator());
    }

    @AfterClass
    public static void tearDownClass() {
        testFactory.stopServerRuntime();
    }

    private static GraphQL createGraphQL(SchemaTranslator translator) {
        GraphQLSchema schema = translator.toGraphQL(testFactory.getServerRuntime().getChannel().getEntityResolver());
        return new GraphQL(schema);
    }

    private static SchemaTranslator createSchemaTranslator() {
        return new DefaultSchemaTranslator(testFactory.getObjectContext());
    }

    String post_graphql_request(String request) {
        System.out.println(request);
        Object r = graphQL.execute(request).getData();
        return r == null ? "" : r.toString();
    }
}
