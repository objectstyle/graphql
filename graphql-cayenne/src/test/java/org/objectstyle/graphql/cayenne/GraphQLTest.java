package org.objectstyle.graphql.cayenne;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.CapsStrategy;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SelectQuery;

import org.junit.Test;

import org.objectstyle.graphql.cayenne.orm.CustomQueryDataFetcher;
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
                .builder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void incudeOneEntityStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .includeEntities("E1")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
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
                .builder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .includeEntities(E2.class)
                .includeEntities(E3.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void incudeAllEntitiesStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .includeEntities("E1")
                .includeEntities("E2")
                .includeEntities("E3")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
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
                .builder(testFactory.getObjectContext())
                .excludeEntities(E2.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void excludeOneEntityStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .excludeEntities("E2")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
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
                .builder(testFactory.getObjectContext())
                .excludeEntities(E1.class)
                .excludeEntities(E2.class)
                .excludeEntities(E3.class)
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();


        excludeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void excludeAllEntitiesStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .excludeEntities("E1")
                .excludeEntities("E2")
                .excludeEntities("E3")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
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
                .builder(testFactory.getObjectContext())
                .includeEntityProperty(E1.class, "id")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void includeEntityProperyStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .includeEntityProperty("E1", "id")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .excludeEntityProperty(E1.class, "name")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .excludeEntityProperty("E1", "name")
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void dataFetcherStringTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .dataFetcher("E1", DefaultDataFetcher.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1, name=a}, {id=2, name=b}]}");
    }

    @Test
    public void dataFetcherClassTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .dataFetcher(E1.class, DefaultDataFetcher.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{E1=[{id=1, name=a}, {id=2, name=b}]}");
    }

    private void customQuery(GraphQL graphQL){
        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=1, name=a}]}");
    }

    @Test
    public void selectQueryTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        Map<String, Object> params = new HashMap<>();

        params.put("id", 1);

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .build();

        customQuery(new GraphQL(schema));
    }

    @Test
    public void selectQueryDataFetcherTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        Map<String, Object> params = new HashMap<>();

        params.put("id", 1);

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);

        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .customQueryDataFetcher("testE1", CustomQueryDataFetcher.class)
                .build();

        customQuery(new GraphQL(schema));
    }

    @Test
    public void selectQueryExpressionTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();


        SelectQuery<?> query = new SelectQuery<>("E1", ExpressionFactory.exp("id = $id"));

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=2, name=b}]}");
    }

    @Test
    public void objectSelectTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        ObjectSelect query = ObjectSelect.query(E1.class).where(E1.ID.eq(2));

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=2, name=b}]}");
    }

    @Test
    public void objectSelectExpressionTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        ObjectSelect query = ObjectSelect.query(E1.class).where(ExpressionFactory.exp("id = $id"));

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=2, name=b}]}");
    }

    @Test
    public void sqlSelectExpressionTest() {
        EntityBuilder entityBuilder = EntityBuilder
                .builder(testFactory.getObjectContext())
                .build();

        SQLSelect<?> query = SQLSelect.query(E1.class,
                "SELECT * FROM utest.E1 WHERE ID = #bind($id)").columnNameCaps(CapsStrategy.LOWER);

        query.params("id", "$id");

        GraphQLSchema schema = SchemaBuilder.builder(entityBuilder)
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals(r.getData().toString(), "{testE1=[{id=2, name=b}]}");
    }
}
