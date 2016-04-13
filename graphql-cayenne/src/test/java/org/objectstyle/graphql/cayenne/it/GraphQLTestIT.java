package org.objectstyle.graphql.cayenne.it;

import org.objectstyle.graphql.test.TestCases;

public class GraphQLTestIT extends TestCases {
    private static GraphQLTestFactory testfactory = new GraphQLTestFactory();

    @Override
    protected String postGraphqlRequest(String request) {
        String r = testfactory.post_graphql_request(request);
        if(r == null) {
            return null;
        }

        return ("{data:" + r + "}").replace(" ", "").replace("=", ":") ;
    }
}
