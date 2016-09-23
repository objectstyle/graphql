package org.objectstyle.graphql.bootique;

import org.junit.ClassRule;
import org.objectstyle.graphql.test.TestCases;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class GraphQLTestIT extends TestCases {
    @ClassRule
    public static GraphQLApp app = new GraphQLApp();

    @Override
    protected String postGraphqlRequest(String query) {
        Response r = app.postGraphQL(query.replace("\"", "\\\""));
        if(r.getStatus() != Status.OK.getStatusCode())
            return null;

        return  r.readEntity(String.class).replace("\"", "");
    }

    @Override
    protected String postGraphqlRequest(String query, String variables) {
        Response r = app.postGraphQL(query.replace("\"", "\\\""), variables.replace("\"", "\\\""));
        if(r.getStatus() != Status.OK.getStatusCode())
            return null;

        return  r.readEntity(String.class).replace("\"", "");
    }
}
