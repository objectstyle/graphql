package org.objectstyle.graphql.cayenne;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import org.objectstyle.graphql.cayenne.orm.DefaultDataFetcher;
import org.objectstyle.graphql.cayenne.orm.SchemaBuilder;

import org.objectstyle.graphql.test.TestFactory;
import org.objectstyle.graphql.test.cayenne.E1;
import org.objectstyle.graphql.test.cayenne.E2;

import org.objectstyle.graphql.test.cayenne.E3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static graphql.ErrorType.ValidationError;
import static org.junit.Assert.assertEquals;

public class GraphQLTest {
    private static TestFactory testFactory = new TestFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLTest.class);

    @Test
    public void incudeOneEntityTest(){
        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class)
                .includeEntities(E1.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1, name=a}]}");

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E2 is undefined");


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E3 is undefined");
    }

    @Test
    public void incudeAllEntitiesTest(){
        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class)
                .includeEntities(E1.class)
                .includeEntities(E2.class)
                .includeEntities(E3.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1, name=a}]}");

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E2=[{id=3, name=c}]}");


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E3=[{id=6, name=f}]}");
    }

    @Test
    public void excludeOneEntityTest(){
        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class)
                .excludeEntities(E2.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1, name=a}]}");

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E2 is undefined");


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E3=[{id=6, name=f}]}");
    }

    @Test
    public void excludeAllEntitiesTest(){
        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class)
                .excludeEntities(E1.class)
                .excludeEntities(E2.class)
                .excludeEntities(E3.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E1 is undefined");

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E2 is undefined");


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        assertEquals(r.getData(), null);
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field E3 is undefined");
    }

    @Test
    public void testCustomQuery()
    {
        SchemaBuilder.Builder schemaBuilder = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class);

        schemaBuilder.build();

        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");
        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        ExecutionResult r = schemaBuilder.query("E1", query);
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "[{id=1, name=a}]");
        System.out.println(r.getData());
    }

    @Test
    public void testCustomQuery1()
    {
        SchemaBuilder.Builder schemaBuilder = SchemaBuilder.newSchemaBuilder()
                .objectContext(testFactory.getObjectContext())
                .dataFetcher(DefaultDataFetcher.class);

        schemaBuilder.build();

        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");
        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        ExecutionResult r = schemaBuilder.query("E1", query, "name");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "[{name=a}]");
        System.out.println(r.getData());
    }
}
