package org.objectstyle.graphql.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;

import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * A REST resource exposing the GraphQL engine.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GraphQLResource {

	@Inject
	private GraphQL graphql;

	@POST
	public ExecutionResult execute(GraphQLRestQuery queryHolder) {

		if (queryHolder == null || queryHolder.getQuery() == null) {
			throw new BQGraphQLRestException(Status.BAD_REQUEST, "No query");
		}

		return graphql.execute(queryHolder.getQuery());
	}
}
