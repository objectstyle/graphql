package org.objectstyle.graphql.rest;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import graphql.GraphQL;
import org.objectstyle.graphql.rest.json.JacksonReaderWriter;
import org.objectstyle.graphql.rest.provider.GraphQLRestExceptionMapper;
import org.objectstyle.graphql.rest.provider.ExecutionResultWriter;
import org.objectstyle.graphql.rest.provider.GraphQLRestQueryReader;
import org.objectstyle.graphql.rest.provider.MessageResponseWriter;

/**
 * Loads JAX-RS extensions for GraphQL processing.
 */

public class GraphQLFeature implements Feature {

    private GraphQL graphQL;
    private JacksonReaderWriter jacksonReaderWriter;

    @Override
    public boolean configure(FeatureContext context) {
        context.register(MessageResponseWriter.class);
        context.register(GraphQLRestExceptionMapper.class);
        context.register(GraphQLRestQueryReader.class);
        context.register(ExecutionResultWriter.class);
        context.register(GraphQLResource.class);

        context.property(GraphQL.class.getName(), graphQL);
        context.property(JacksonReaderWriter.class.getName(), jacksonReaderWriter);

        return true;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLFeature graphQLFeature;


        private Builder() {
            this.graphQLFeature = new GraphQLFeature();
        }

        public Builder graphQL(GraphQL graphQL) {
            graphQLFeature.graphQL = graphQL;
            return this;
        }

        public Builder jacksonReaderWriter(JacksonReaderWriter jacksonReaderWriter) {
            graphQLFeature.jacksonReaderWriter = jacksonReaderWriter;
            return this;
        }

        public GraphQLFeature build() {
            return graphQLFeature;
        }
    }
}