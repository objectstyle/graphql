package org.objectstyle.cayenne.graphql.jaxrs.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.objectstyle.cayenne.graphql.json.JsonWriter;

import com.google.inject.Inject;

import graphql.ExecutionResult;

@Provider
@Singleton
public class ExecutionResultWriter implements MessageBodyWriter<ExecutionResult> {

	@Inject
	private JsonWriter writer;

	@Override
	public long getSize(ExecutionResult t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ExecutionResult.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ExecutionResult t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException, WebApplicationException {

		writer.write(entityStream, generator -> {

			generator.writeStartObject();

			// TODO: is it ok to rely on default serialization of data and
			// errors?

			if (t.getData() != null) {
				generator.writeObjectField("data", t.getData());
			}

			if (t.getErrors() != null && !t.getErrors().isEmpty()) {
				generator.writeObjectField("errors", t.getErrors());
			}

			generator.writeEndObject();

		});
	}
}
