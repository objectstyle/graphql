package org.objectstyle.graphql.rest.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.objectstyle.graphql.rest.MessageResponse;
import org.objectstyle.graphql.rest.json.JsonWriter;

import com.google.inject.Inject;

public class MessageResponseWriter implements MessageBodyWriter<MessageResponse> {

	@Inject
	private JsonWriter writer;

	@Override
	public long getSize(MessageResponse t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MessageResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(MessageResponse t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		writer.write(entityStream, generator -> {
			generator.writeStartObject();

			if (t.getMessage() != null) {
				generator.writeStringField("message", t.getMessage());
			}

			generator.writeEndObject();
		});

	}

}
