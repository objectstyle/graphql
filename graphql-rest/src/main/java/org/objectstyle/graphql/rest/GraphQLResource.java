package org.objectstyle.graphql.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;

import java.io.IOException;
import java.util.HashMap;

/**
 * A REST resource exposing the GraphQL engine.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GraphQLResource {
    @Context
    Configuration config;

    @POST
    public ExecutionResult execute(GraphQLRestQuery queryHolder) {

        if (queryHolder == null || queryHolder.getQuery() == null) {
            throw new GraphQLRestException(Status.BAD_REQUEST, "No query");
        }

        GraphQL graphql = (GraphQL) config.getProperty(GraphQL.class.getName());

        if(queryHolder.getVariables() != null) {
            HashMap variables = new HashMap<>();

            try {
                variables = new ObjectMapper().readValue(queryHolder.getVariables(), HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return graphql.execute(queryHolder.getQuery(), (Object)null, variables);
        }

        return graphql.execute(queryHolder.getQuery());
    }
}
