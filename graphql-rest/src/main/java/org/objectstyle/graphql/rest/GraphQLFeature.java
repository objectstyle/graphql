package org.objectstyle.graphql.rest;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.objectstyle.graphql.rest.provider.GraphQLRestExceptionMapper;
import org.objectstyle.graphql.rest.provider.ExecutionResultWriter;
import org.objectstyle.graphql.rest.provider.GraphQLRestQueryReader;
import org.objectstyle.graphql.rest.provider.MessageResponseWriter;

/**
 * Loads JAX-RS extensions for GraphQL processing.
 */

public class GraphQLFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(MessageResponseWriter.class);
        context.register(GraphQLRestExceptionMapper.class);
        context.register(GraphQLRestQueryReader.class);
        context.register(ExecutionResultWriter.class);
        context.register(GraphQLResource.class);
        return true;
    }
}