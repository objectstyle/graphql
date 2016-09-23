package org.objectstyle.graphql.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TestCases {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TestCases.class);

    protected abstract String postGraphqlRequest(String query);
    protected abstract String postGraphqlRequest(String query, String variables);

    @Test
    public void testSchemaQuery() {
        String r = postGraphqlRequest("{ __schema { types { name }}}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{__schema:{types:[{name:"));
    }

    @Test
    public void testQueryAllE1() {
        String r = postGraphqlRequest("{ E1 { id name }}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:"));
    }

    @Test
    public void testQueryAllE2() throws InterruptedException {
        String r = postGraphqlRequest("{ E2 { id name }}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E2:[{id:"));
    }

    @Test
    public void testQueryAllE3() throws InterruptedException {
        String r = postGraphqlRequest("{ E3 { id name }}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E3:[{id:"));
    }

    @Test
    public void testDataQueryByIdForE1() {
        String r = postGraphqlRequest("{ E1 (id:2) { id name}}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b}]}}", r);
    }

    @Test
    public void testDataQueryByIdForE2() {
        String r = postGraphqlRequest("{ E2 (id:3) { id name}}");

        LOGGER.info(r);
        assertEquals("{data:{E2:[{id:3,name:c}]}}", r);
    }

    @Test
    public void testDataQueryRelationshipsForE1() {
        String r = postGraphqlRequest("{ E1 (id:2 name:\"b\") { id name e2s {id name} }}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:2,name:b,e2s:[{id:"));
    }

    @Test
    public void testDataQueryRelationshipsForE1_2() {
        String r = postGraphqlRequest("{ E1 (id:2 name:\"b\") { id name e2s (id:4) {id name} }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b,e2s:[{id:4,name:d}]}]}}", r);
    }

    @Test
    public void testDataQueryRelationshipsForE2() {
        String r = postGraphqlRequest("{ E2 (id:3) { id name e1 {id name} }}");

        LOGGER.info(r);
        assertEquals("{data:{E2:[{id:3,name:c,e1:{id:1,name:a}}]}}", r);
    }

    @Test
    public void testDataQueryRelationshipsForE2_1() {
        String r = postGraphqlRequest("{E2 (id:4) {id name e1 {id name e2s {id name e3s {id e2 {id name}} }}}}");

        LOGGER.info(r);
        assertEquals("{data:{E2:[{id:4,name:d,e1:{id:2,name:b,e2s:[{id:4,name:d,e3s:[{id:6,e2:{id:4,name:d}},{id:7,e2:{id:4,name:d}}]},{id:5,name:e,e3s:[{id:8,e2:{id:5,name:e}}]}]}}]}}", r);
    }

    @Test
    public void testDataQueryRelationshipsForE3() {
        String r = postGraphqlRequest("{ E3 (id:6) { id name e2 {id name e1 {id name}} }}");

        LOGGER.info(r);
        assertEquals("{data:{E3:[{id:6,name:f,e2:{id:4,name:d,e1:{id:2,name:b}}}]}}", r);
    }

    @Test
    public void testDataQueryRelationshipsForE3_2() {
        String r = postGraphqlRequest("{ E1 (id:2 name:\"b\") { id name e2s {id name e3s {id name}} }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b,e2s:[{id:4,name:d,e3s:[{id:6,name:f},{id:7,name:g}]},{id:5,name:e,e3s:[{id:8,name:h}]}]}]}}", r);
    }

    @Test
    public void testDataQueryAlias_1() {
        String r = postGraphqlRequest("{ E1 (id:2 name:\"b\") { id name e4: e2s(id:4) {id name} }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b,e4:[{id:4,name:d}]}]}}", r);
    }

    @Test
    public void testDataQueryAlias_2() {
        String r = postGraphqlRequest("{e1: E1(id:1) {id} e2: E1(id:2) {id}}");

        LOGGER.info(r);
        assertEquals("{data:{e1:[{id:1}],e2:[{id:2}]}}", r);
    }

    @Test
    public void testQueryFiltersAscending() {
        String r = postGraphqlRequest("{ E1(_first:\"1\" _ascending:\"id\") { id name }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:1,name:a}]}}", r);
    }

    @Test
    public void testQueryFiltersDescending_1() {
        String r = postGraphqlRequest("{ E1(_first:\"1\" _descending:[\"id\"]) { id name }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b}]}}", r);
    }

    @Test
    public void testQueryFiltersDescending_2() {
        String r = postGraphqlRequest("{ E1 (id:2 name:\"b\") { id name e2s(_descending:[\"id\"]) {id name} }}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b,e2s:[{id:5,name:e},{id:4,name:d}]}]}}", r);
    }

    @Test
    public void testFragments() {
        String r = postGraphqlRequest("{ E1 (id:2) { ...fragmentTest } } fragment fragmentTest on E1 {id name}");

        LOGGER.info(r);
        assertEquals("{data:{E1:[{id:2,name:b}]}}", r);
    }

    @Test
    public void testQueryWithVariables() {
        String r = postGraphqlRequest("($id: Int!) { E1(id:$id) { id name }}", "{\"id\":1}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:1,name:a}]}}"));
    }

    @Test
    public void testQueryWithIncludeDirectiveFalse() {
        String r = postGraphqlRequest("($includeE2s: Boolean!) { E1(id: 1) {id e2s @include(if: $includeE2s) { name }}}", "{\"includeE2s\":false}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:1}]}}"));
    }

    @Test
    public void testQueryWithIncludeDirectiveTrue() {
        String r = postGraphqlRequest("($includeE2s: Boolean!) { E1(id: 1) {id e2s @include(if: $includeE2s) { name }}}", "{\"includeE2s\":true}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:1,e2s:[{name:c}]}]}}"));
    }

    @Test
    public void testQueryWithSkipDirectiveFalse() {
        String r = postGraphqlRequest("($skipE2s: Boolean!) { E1(id: 1) {id e2s @skip(if: $skipE2s) { name }}}", "{\"skipE2s\":false}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:1,e2s:[{name:c}]}]}}"));
    }

    @Test
    public void testQueryWithSkipDirectiveTrue() {
        String r = postGraphqlRequest("($skipE2s: Boolean!) { E1(id: 1) {id e2s @skip(if: $skipE2s) { name }}}", "{\"skipE2s\":true}");

        LOGGER.info(r);
        assertTrue(r, r.startsWith("{data:{E1:[{id:1}]}}"));
    }
}
