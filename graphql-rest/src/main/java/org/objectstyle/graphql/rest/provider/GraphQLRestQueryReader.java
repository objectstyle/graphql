package org.objectstyle.graphql.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.objectstyle.graphql.rest.GraphQLRestQuery;
import org.objectstyle.graphql.rest.json.JacksonReaderWriter;
import org.objectstyle.graphql.rest.json.JsonReader;

@Singleton
@Provider
public class GraphQLRestQueryReader implements MessageBodyReader<GraphQLRestQuery> {
    @Context
    Configuration config;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return GraphQLRestQuery.class.isAssignableFrom(type);
    }

    @Override
    public GraphQLRestQuery readFrom(Class<GraphQLRestQuery> type, Type genericType, Annotation[] annotations,
                                     MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        JsonReader jsonReader = (JsonReader) config.getProperty(JacksonReaderWriter.class.getName());
        return jsonReader.read(GraphQLRestQuery.class, entityStream);
    }
}
