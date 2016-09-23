package org.objectstyle.graphql.cayenne.it;

import org.objectstyle.graphql.test.TestCases;

public class GraphQLTestIT extends TestCases {
    private static GraphQLTestFactory testfactory = new GraphQLTestFactory();

    @Override
    protected String postGraphqlRequest(String query) {
        String r = testfactory.post_graphql_request(query);
        if(r == null) {
            return null;
        }

        return ("{data:" + r + "}").replace(" ", "").replace("=", ":") ;
    }

    @Override
    protected String postGraphqlRequest(String query, String variables) {
        String r = testfactory.post_graphql_request(query, variables);
        if(r == null) {
            return null;
        }

        return ("{data:" + r + "}").replace(" ", "").replace("=", ":") ;
    }
}
