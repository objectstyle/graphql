package org.objectstyle.cayenne.graphql.jaxrs;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.objectstyle.cayenne.graphql.jaxrs.provider.BQGraphQLRestExceptionMapper;
import org.objectstyle.cayenne.graphql.jaxrs.provider.ExecutionResultWriter;
import org.objectstyle.cayenne.graphql.jaxrs.provider.GraphiQLRestQueryReader;
import org.objectstyle.cayenne.graphql.jaxrs.provider.MessageResponseWriter;

/**
 * Loads JAX-RS extensions for GraphQL processing.
 */
public class GraphQLFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(MessageResponseWriter.class);
		context.register(BQGraphQLRestExceptionMapper.class);
		context.register(GraphiQLRestQueryReader.class);
		context.register(ExecutionResultWriter.class);
		context.register(GraphQLResource.class);
		return true;
	}
}