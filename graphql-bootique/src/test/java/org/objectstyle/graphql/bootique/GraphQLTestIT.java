package org.objectstyle.graphql.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;

import org.apache.cayenne.query.SQLTemplate;
import org.apache.commons.io.FileUtils;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GraphQLTestIT {
    @ClassRule
    public static GraphQLApp app = new GraphQLApp();

    @BeforeClass
    public static void beforeClass() {
        insertTestData();
    }

    private static void insertTestData() {
        app.insert("e1", "id, name", "1, 'a'");
        app.insert("e1", "id, name", "2, 'b'");

        app.insert("e2", "id, name, e1_id", "3, 'c', 1");
        app.insert("e2", "id, name, e1_id", "4, 'd', 2");
        app.insert("e2", "id, name, e1_id", "5, 'e', 2");

        app.insert("e3", "id, name, e2_id", "6, 'f', 4");
        app.insert("e3", "id, name, e2_id", "7, 'g', 4");
        app.insert("e3", "id, name, e2_id", "8, 'h', 5");
    }

    private Response post_graphql_request(String request) {
        return app.postGraphQL(request.replace("\"", "\\\""));
    }

    @Test
    public void testSchemaQuery() {
        Response r = post_graphql_request("{ __schema { types { name }}}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.startsWith("{\"data\":{\"__schema\":{\"types\":[{\"name\":"));
    }

    @Test
    public void testQueryAllE1() {
        Response r = post_graphql_request("{ allE1s { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.startsWith("{\"data\":{\"allE1s\":[{\"id\":"));
    }

    @Test
    public void testQueryAllE1WithArguments() {
        Response r = post_graphql_request("{ allE1s(id:1) { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"allE1s\":[{\"id\":1,\"name\":\"a\"}]}}"));
    }

    @Test
    public void testQueryAllE2() throws InterruptedException {
        Response r = post_graphql_request("{ allE2s { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.startsWith("{\"data\":{\"allE2s\":[{\"id\":"));
    }

    @Test
    public void testQueryAllE3() throws InterruptedException {
        Response r = post_graphql_request("{ allE3s { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.startsWith("{\"data\":{\"allE3s\":[{\"id\":"));
    }

    @Test
    public void testQueryAllE2WithArguments() {
        Response r = post_graphql_request("{ allE2s(name:\"d\") { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"allE2s\":[{\"id\":4,\"name\":\"d\"}]}}"));
    }

    @Test
    public void testDataQueryByIdForE1() {
        Response r = post_graphql_request("{ E1 (id:2) { id name}}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\"}]}}"));
    }

    @Test
    public void testDataQueryByIdForE2() {
        Response r = post_graphql_request("{ E2 (id:3) { id name}}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E2\":[{\"id\":3,\"name\":\"c\"}]}}"));
    }

    @Test
    public void testDataQueryRelationshipsForE1() {
        Response r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s {id name} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.startsWith("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\",\"e2s\":[{\"id\":"));
    }

    @Test
    public void testDataQueryRelationshipsForE1_2() {
        Response r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s (id:4) {id name} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\",\"e2s\":[{\"id\":4,\"name\":\"d\"}]}]}}"));
    }

    @Test
    public void testDataQueryRelationshipsForE2() {
        Response r = post_graphql_request("{ E2 (id:3) { id name e1 {id name} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E2\":[{\"id\":3,\"name\":\"c\",\"e1\":{\"id\":1,\"name\":\"a\"}}]}}"));
    }

    @Test
    public void testDataQueryRelationshipsForE3() {
        Response r = post_graphql_request("{ E3 (id:6) { id name e2 {id name e1 {id name}} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E3\":[{\"id\":6,\"name\":\"f\",\"e2\":{\"id\":4,\"name\":\"d\",\"e1\":{\"id\":2,\"name\":\"b\"}}}]}}"));
    }

    @Test
    public void testDataQueryRelationshipsForE3_2() {
        Response r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s {id name e3s {id name}} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\",\"e2s\":[{\"id\":4,\"name\":\"d\",\"e3s\":[{\"id\":6,\"name\":\"f\"},{\"id\":7,\"name\":\"g\"}]},{\"id\":5,\"name\":\"e\",\"e3s\":[{\"id\":8,\"name\":\"h\"}]}]}]}}"));
    }

    @Test
    public void testDataQueryAlias_1() {
        Response r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e4: e2s(id:4) {id name} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\",\"e4\":[{\"id\":4,\"name\":\"d\"}]}]}}"));
    }

    @Test
    public void testDataQueryAlias_2() {
        Response r = post_graphql_request("{e1: E1(id:1) {id} e2: E1(id:2) {id}}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"e1\":[{\"id\":1}],\"e2\":[{\"id\":2}]}}"));
    }

    @Test
    public void testQueryFiltersAscending() {
        Response r = post_graphql_request("{ allE1s(_first:\"1\" _ascending:\"id\") { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"allE1s\":[{\"id\":1,\"name\":\"a\"}]}}"));
    }

    @Test
    public void testQueryFiltersDescending_1() {
        Response r = post_graphql_request("{ allE1s(_first:\"1\" _descending:[\"id\"]) { id name }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"allE1s\":[{\"id\":2,\"name\":\"b\"}]}}"));
    }

    @Test
    public void testQueryFiltersDescending_2() {
        Response r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s(_descending:[\"id\"]) {id name} }}");
        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        System.out.println(json);
        assertTrue(json, json.equals("{\"data\":{\"E1\":[{\"id\":2,\"name\":\"b\",\"e2s\":[{\"id\":5,\"name\":\"e\"},{\"id\":4,\"name\":\"d\"}]}]}}"));
    }
}
