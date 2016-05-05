package org.objectstyle.graphql.bootique;

import com.google.inject.Inject;
import com.google.inject.Provider;
import graphql.GraphQL;
import org.objectstyle.graphql.rest.GraphQLFeature;
import org.objectstyle.graphql.rest.json.JacksonReaderWriter;

class GraphQLFeatureProvider implements Provider<GraphQLFeature> {
    @Inject
    private GraphQL graphQL;

    @Inject
    private JacksonReaderWriter jacksonReaderWriter;

    @Override
    public GraphQLFeature get() {
        return GraphQLFeature.newBuilder().graphQL(graphQL).jacksonReaderWriter(jacksonReaderWriter).build();
    }
}
