package org.objectstyle.graphql.cayenne;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.*;

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

    private void incudeOneEntityTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E2 is undefined", r.getErrors().get(0).getMessage());


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E3 is undefined", r.getErrors().get(0).getMessage());
    }

    @Test
    public void incudeOneEntityClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void incudeOneEntityStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntities("E1")
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    private void incudeAllEntitiesTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E2=[{id=3, name=c}]}", r.getData().toString());


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E3=[{id=6, name=f}]}", r.getData().toString());
    }

    @Test
    public void incudeAllEntitiesClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntities(E1.class)
                .includeEntities(E2.class)
                .includeEntities(E3.class)
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void incudeAllEntitiesStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntities("E1")
                .includeEntities("E2")
                .includeEntities("E3")
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    private void excludeOneEntityTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E2 is undefined", r.getErrors().get(0).getMessage());


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E3=[{id=6, name=f}]}", r.getData().toString());
    }

    @Test
    public void excludeOneEntityClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntities(E2.class)
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void excludeOneEntityStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntities("E2")
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    private void excludeAllEntitiesTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E1 is undefined", r.getErrors().get(0).getMessage());

        r = graphQL.execute("query { E2 (id:3) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E2 is undefined", r.getErrors().get(0).getMessage());


        r = graphQL.execute("query { E3 (id:6) { id name}}");
        assertEquals(null, r.getData());
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field E3 is undefined", r.getErrors().get(0).getMessage());
    }

    @Test
    public void excludeAllEntitiesClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntities(E1.class)
                .excludeEntities(E2.class)
                .excludeEntities(E3.class)
                .build();

        excludeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void excludeAllEntitiesStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntities("E1")
                .excludeEntities("E2")
                .excludeEntities("E3")
                .build();

        excludeAllEntitiesTest(new GraphQL(schema));
    }

    private void entityProperyTest(GraphQL graphQL) {
        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1}]}", r.getData().toString());

        r = graphQL.execute("query { E1 (id:1) { id name}}");
        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type FieldUndefined: Field name is undefined", r.getErrors().get(0).getMessage());
    }

    @Test
    public void includeEntityProperyClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntityProperty(E1.class, "id")
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void includeEntityProperyStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .includeEntityProperty("E1", "id")
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntityProperty(E1.class, "name")
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .excludeEntityProperty("E1", "name")
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void dataFetcherStringTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .dataFetcher("E1", DefaultDataFetcher.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}, {id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void dataFetcherClassTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .dataFetcher(E1.class, DefaultDataFetcher.class)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}, {id=2, name=b}]}", r.getData().toString());
    }

    private void customQuery(GraphQL graphQL){
        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=1, name=a}]}", r.getData().toString());
    }

    @Test
    public void selectQueryTest() {
        Map<String, Object> params = new HashMap<>();

        params.put("id", 1);

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>("E1", expression);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        customQuery(new GraphQL(schema));
    }

    @Test
    public void selectQueryExpressionTest() {
        SelectQuery<?> query = new SelectQuery<>("E1", ExpressionFactory.exp("id = $id"));

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void objectSelectTest() {
        ObjectSelect query = ObjectSelect.query(E1.class).where(E1.ID.eq(2));

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void objectSelectExpressionTest() {
        ObjectSelect query = ObjectSelect.query(E1.class).where(ExpressionFactory.exp("id = $id"));

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void sqlSelectExpressionTest() {
        SQLSelect<?> query = SQLSelect.query(E1.class,
                "SELECT * FROM utest.E1 WHERE ID = #bind($id)").columnNameCaps(CapsStrategy.LOWER);

        query.params("id", "$id");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void selectByIdTest() {
        SelectById query = SelectById.query(E1.class, 2);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .query("testE1", query)
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }


    @Test
    public void mutationUpdateTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("mutation  { updateE1(id:1 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=1, name=bb}]}", r.getData().toString());

        r = graphQL.execute("query  { E1(id:1 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=bb}]}", r.getData().toString());

        r = graphQL.execute("mutation  { updateE1(id:1 name:\"a\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query  { E1(id:1 name:\"a\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());
    }

    @Test
    public void mutationAddTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("mutation  { updateE1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=331, name=bb}]}", r.getData().toString());

        r = graphQL.execute("query  { E1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=331, name=bb}]}", r.getData().toString());
    }

    @Test
    public void mutationDeleteTest() {
        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("mutation  { updateE1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=331, name=bb}]}", r.getData().toString());

        r = graphQL.execute("query  { E1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=331, name=bb}]}", r.getData().toString());

        r = graphQL.execute("mutation  { deleteE1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{deleteE1=[{id=331, name=bb}]}", r.getData().toString());

        r = graphQL.execute("query  { E1(id:331 name:\"bb\") {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[]}", r.getData().toString());
    }
}
