package org.objectstyle.graphql.bootique;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;

import org.objectstyle.graphql.test.TestCases;

public class GraphQLTestIT extends TestCases {
    @ClassRule
    public static GraphQLApp app = new GraphQLApp();

    @Override
    protected String postGraphqlRequest(String request) {
        Response r = app.postGraphQL(request.replace("\"", "\\\""));
        if(r.getStatus() != Status.OK.getStatusCode())
            return null;

        return  r.readEntity(String.class).replace("\"", "");
    }
}
