package org.objectstyle.graphql.cayenne;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import org.objectstyle.graphql.cayenne.orm.DefaultDataFetcher;
import org.objectstyle.graphql.cayenne.orm.EntityBuilder;
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

    private void incudeOneEntityTest(GraphQL graphQL) {
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
    public void incudeOneEntityClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void incudeOneEntityStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntities("E1")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    private void incudeAllEntitiesTest(GraphQL graphQL) {
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
    public void incudeAllEntitiesClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .includeEntities(E2.class)
                .includeEntities(E3.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void incudeAllEntitiesStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntities("E1")
                .includeEntities("E2")
                .includeEntities("E3")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    private void excludeOneEntityTest(GraphQL graphQL) {
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
    public void excludeOneEntityClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntities(E2.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void excludeOneEntityStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntities("E2")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    private void excludeAllEntitiesTest(GraphQL graphQL) {
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
    public void excludeAllEntitiesClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntities(E1.class)
                .excludeEntities(E2.class)
                .excludeEntities(E3.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();


        excludeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void excludeAllEntitiesStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntities("E1")
                .excludeEntities("E2")
                .excludeEntities("E3")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        excludeAllEntitiesTest(new GraphQL(schema));
    }

    private void entityProperyTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id }}");
        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1}]}");

        r = graphQL.execute("query { E1 (id:1) { id name}}");
        assertEquals(r.getErrors().get(0).getErrorType(), ValidationError);
        assertEquals(r.getErrors().get(0).getMessage(), "Validation error of type FieldUndefined: Field name is undefined");
    }

    @Test
    public void includeEntityProperyClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntityProperty(E1.class, "id")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void includeEntityProperyStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .includeEntityProperty("E1", "id")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntityProperty(E1.class, "name")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .excludeEntityProperty("E1", "name")
                .build();

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void customQueryTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .newEntityBuilder(testFactory.getObjectContext())
                .build();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("id", 1);

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        GraphQLSchema schema = SchemaBuilder.newSchemaBuilder(entityBuilder)
                .dataFetcher(DefaultDataFetcher.class)
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=1, name=a}]}");
    }
}
