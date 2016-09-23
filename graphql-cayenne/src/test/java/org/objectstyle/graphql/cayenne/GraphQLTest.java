package org.objectstyle.graphql.cayenne;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.*;

import org.junit.Test;

import org.objectstyle.graphql.cayenne.orm.*;

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
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, E1.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        incudeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void incudeOneEntityStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());
        queryType.configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E1");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
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
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, E1.class)
                .configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, E2.class)
                .configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, E3.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        incudeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void incudeAllEntitiesStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());


        queryType.configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E1")
                .configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E2")
                .configureEntities(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E3");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
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
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, E2.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        excludeOneEntityTest(new GraphQL(schema));
    }

    @Test
    public void excludeOneEntityStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E2");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
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
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, E1.class)
                .configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, E2.class)
                .configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, E3.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        excludeAllEntitiesTest(new GraphQL(schema));
    }

    @Test
    public void excludeAllEntitiesStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E1")
                .configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E2")
                .configureEntities(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E3");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
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
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureProperties(EntityBuilder.ConfigureType.INCLUDE_OBJECT, E1.class, "id");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void includeEntityProperyStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureProperties(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E1", "id");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyClassTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureProperties(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, E1.class, "name");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void excludeEntityProperyStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureProperties(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E1", "name");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        entityProperyTest(new GraphQL(schema));
    }

    @Test
    public void dataFetcherStringTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .dataFetcher("E1", DefaultDataFetcher.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}, {id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void dataFetcherClassTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .dataFetcher(E1.class, DefaultDataFetcher.class);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}, {id=2, name=b}]}", r.getData().toString());
    }

    private void customQuery(GraphQL graphQL) {
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

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        customQuery(new GraphQL(schema));
    }

    @Test
    public void selectQueryExpressionTest() {
        SelectQuery<?> query = new SelectQuery<>("E1", ExpressionFactory.exp("id = $id"));

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void objectSelectTest() {
        ObjectSelect query = ObjectSelect.query(E1.class).where(E1.ID.eq(2));

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void objectSelectExpressionTest() {
        ObjectSelect query = ObjectSelect.query(E1.class).where(ExpressionFactory.exp("id = $id"));

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
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

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 (id:2) { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }

    @Test
    public void selectByIdTest() {
        SelectById query = SelectById.query(E1.class, 2);

        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext())
                .query("testE1", query);

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { testE1 { id name }}");

        LOGGER.info(r.getData().toString());
        assertEquals("{testE1=[{id=2, name=b}]}", r.getData().toString());
    }


    @Test
    public void mutationUpdateTest() {
        MutationType.Builder mutationType = MutationType.builder(testFactory.getObjectContext());

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext()).mutationType(mutationType.build())
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
        MutationType.Builder mutationType = MutationType.builder(testFactory.getObjectContext());

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext()).mutationType(mutationType.build())
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
    }

    @Test
    public void mutationDeleteTest() {
        MutationType.Builder mutationType = MutationType.builder(testFactory.getObjectContext());

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext()).mutationType(mutationType.build())
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

    @Test
    public void mutationIncludePropertyTest() {
        MutationType.Builder mutationType = MutationType.builder(testFactory.getObjectContext());

        mutationType.configureProperties(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E1", "id");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext()).mutationType(mutationType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("mutation  { updateE1(id:331) {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=331, name=null}]}", r.getData().toString());

        r = graphQL.execute("mutation  { deleteE1(id:331) {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{deleteE1=[{id=331, name=null}]}", r.getData().toString());
    }

    @Test
    public void mutationExcludePropertyTest() {
        MutationType.Builder mutationType = MutationType.builder(testFactory.getObjectContext());

        mutationType.configureProperties(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E1", "name");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext()).mutationType(mutationType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("mutation  { updateE1(id:331) {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{updateE1=[{id=331, name=null}]}", r.getData().toString());

        r = graphQL.execute("mutation  { deleteE1(id:331) {id name} }}");
        LOGGER.info(r.getData().toString());
        assertEquals("{deleteE1=[{id=331, name=null}]}", r.getData().toString());
    }

    @Test
    public void includeArgumentTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureArguments(EntityBuilder.ConfigureType.INCLUDE_OBJECT, "E1", "id");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query { E1 (name:\"a\") { id name}}");

        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type UnknownArgument: Unknown argument name", r.getErrors().get(0).getMessage());
    }

    @Test
    public void excludeArgumentTest() {
        QueryType.Builder queryType = QueryType.builder(testFactory.getObjectContext());

        queryType.configureArguments(EntityBuilder.ConfigureType.EXCLUDE_OBJECT, "E1", "name");

        GraphQLSchema schema = SchemaBuilder.builder(testFactory.getObjectContext())
                .queryType(queryType.build())
                .build();

        GraphQL graphQL = new GraphQL(schema);

        ExecutionResult r = graphQL.execute("query { E1 (id:1) { id name}}");
        LOGGER.info(r.getData().toString());
        assertEquals("{E1=[{id=1, name=a}]}", r.getData().toString());

        r = graphQL.execute("query { E1 (name:\"a\") { id name}}");

        assertEquals(ValidationError, r.getErrors().get(0).getErrorType());
        assertEquals("Validation error of type UnknownArgument: Unknown argument name", r.getErrors().get(0).getMessage());
    }
}
