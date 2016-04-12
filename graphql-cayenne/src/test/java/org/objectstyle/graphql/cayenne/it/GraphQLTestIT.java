package org.objectstyle.graphql.cayenne.it;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class GraphQLTestIT extends GraphQLTestFactory {
    @BeforeClass
    public static void beforeClass() {
        insertTestData();
    }

    private static void insertTestData() {
        insert("e1", "id, name", "1, 'a'");
        insert("e1", "id, name", "2, 'b'");

        insert("e2", "id, name, e1_id", "3, 'c', 1");
        insert("e2", "id, name, e1_id", "4, 'd', 2");
        insert("e2", "id, name, e1_id", "5, 'e', 2");

        insert("e3", "id, name, e2_id", "6, 'f', 4");
        insert("e3", "id, name, e2_id", "7, 'g', 4");
        insert("e3", "id, name, e2_id", "8, 'h', 5");
    }

    @Test
    public void testSchemaQuery() {
        String r = post_graphql_request("{ __schema { types { name }}}");

        System.out.println(r);
        assertTrue(r, r.startsWith("{__schema={types="));
    }

    @Test
    public void testQueryAllE1() {
        String r = post_graphql_request("{ allE1s { id name }}");

        System.out.println(r);
        assertTrue(r, r.startsWith("{allE1s=[{id"));
    }

    @Test
    public void testQueryAllE1WithArguments() {
        String r = post_graphql_request("{ allE1s(id:1) { id name }}");

        System.out.println(r);
        assertTrue(r, r.equals("{allE1s=[{id=1, name=a}]}"));
    }

    @Test
    public void testQueryAllE2() {
        String r = post_graphql_request("{ allE2s { id name }}");

        System.out.println(r);
        assertTrue(r, r.startsWith("{allE2s=[{id="));
    }

    @Test
    public void testQueryAllE3() throws InterruptedException {
        String r = post_graphql_request("{ allE3s { id name }}");

        System.out.println(r);
        assertTrue(r, r.startsWith("{allE3s=[{id="));
    }

    @Test
    public void testQueryAllE2WithArguments() {
        String r = post_graphql_request("{ allE2s(name:\"d\") { id name }}");

        System.out.println(r);
        assertTrue(r, r.equals("{allE2s=[{id=4, name=d}]}"));
    }

    @Test
    public void testDataQueryByIdForE1() {
        String r = post_graphql_request("{ E1 (id:2) { id name}}");

        System.out.println(r);
        assertTrue(r, r.equals("{E1=[{id=2, name=b}]}"));
    }

    @Test
    public void testDataQueryByIdForE2() {
        String r = post_graphql_request("{ E2 (id:3) { id name}}");

        System.out.println(r);
        assertTrue(r, r.equals("{E2=[{id=3, name=c}]}"));
    }

    @Test
    public void testDataQueryRelationshipsForE1() {
        String r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s {id name} }}");

        System.out.println(r);
        assertTrue(r, r.startsWith("{E1=[{id=2, name=b, e2s=[{id"));
    }

    @Test
    public void testDataQueryRelationshipsForE1_2() {
        String r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s (id:4) {id name} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E1=[{id=2, name=b, e2s=[{id=4, name=d}]}]}"));
    }

    @Test
    public void testDataQueryRelationshipsForE2() {
        String r = post_graphql_request("{ E2 (id:3) { id name e1 {id name} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E2=[{id=3, name=c, e1={id=1, name=a}}]}"));
    }

    @Test
    public void testDataQueryRelationshipsForE3() {
        String r = post_graphql_request("{ E3 (id:6) { id name e2 {id name e1 {id name}} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E3=[{id=6, name=f, e2={id=4, name=d, e1={id=2, name=b}}}]}"));
    }

    @Test
    public void testDataQueryRelationshipsForE3_2() {
        String r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s {id name e3s {id name}} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E1=[{id=2, name=b, e2s=[{id=4, name=d, e3s=[{id=6, name=f}, {id=7, name=g}]}, {id=5, name=e, e3s=[{id=8, name=h}]}]}]}"));
    }

    @Test
    public void testDataQueryAlias_1() {
        String r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e4: e2s(id:4) {id name} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E1=[{id=2, name=b, e4=[{id=4, name=d}]}]}"));
    }

    @Test
    public void testDataQueryAlias_2() {
        String r = post_graphql_request("{e1: E1(id:1) {id} e2: E1(id:2) {id}}");

        System.out.println(r);
        assertTrue(r, r.equals("{e1=[{id=1}], e2=[{id=2}]}"));
    }

    @Test
    public void testQueryFiltersAscending() {
        String r = post_graphql_request("{ allE1s(_first:\"1\" _ascending:\"id\") { id name }}");

        System.out.println(r);
        assertTrue(r, r.equals("{allE1s=[{id=1, name=a}]}"));
    }

    @Test
    public void testQueryFiltersDescending_1() {
        String r = post_graphql_request("{ allE1s(_first:\"1\" _descending:[\"id\"]) { id name }}");

        System.out.println(r);
        assertTrue(r, r.equals("{allE1s=[{id=2, name=b}]}"));
    }

    @Test
    public void testQueryFiltersDescending_2() {
        String r = post_graphql_request("{ E1 (id:2 name:\"b\") { id name e2s(_descending:[\"id\"]) {id name} }}");

        System.out.println(r);
        assertTrue(r, r.equals("{E1=[{id=2, name=b, e2s=[{id=5, name=e}, {id=4, name=d}]}]}"));
    }
}
