package org.objectstyle.cayenne.graphql.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.objectstyle.cayenne.graphql.jaxrs.GraphQLRestQuery;
import org.objectstyle.cayenne.graphql.json.JsonReader;

import com.google.inject.Inject;

@Singleton
@Provider
public class GraphQLRestQueryReader implements MessageBodyReader<GraphQLRestQuery> {

	@Inject
	private JsonReader jsonParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return GraphQLRestQuery.class.isAssignableFrom(type);
	}

	@Override
	public GraphQLRestQuery readFrom(Class<GraphQLRestQuery> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException, WebApplicationException {

		return jsonParser.read(GraphQLRestQuery.class, entityStream);
	}
}
