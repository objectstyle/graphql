package org.objectstyle.graphql.rest.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import javax.ws.rs.core.Response.Status;

import org.objectstyle.graphql.rest.GraphQLRestException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

public class JacksonReaderWriter implements JsonReader, JsonWriter {

	private ObjectMapper sharedMapper;
	private JsonFactory sharedFactory;

	public JacksonReaderWriter() {

		// fun Jackson API with circular dependencies ... so we create a mapper
		// first, and grab implicitly created factory from it
		this.sharedMapper = new ObjectMapper();
		this.sharedFactory = sharedMapper.getFactory();

		// make sure mapper does not attempt closing streams it does not
		// manage... why is this even a default in jackson?
		sharedFactory.disable(Feature.AUTO_CLOSE_TARGET);

		// do not flush every time. why would we want to do that?
		// this is having a HUGE impact on extrest serializers (5x speedup)
		sharedMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
	}

	private JsonNode parseJson(InputStream jsonStream) {
		Objects.requireNonNull(jsonStream);

		try {
			com.fasterxml.jackson.core.JsonParser parser = sharedFactory.createParser(jsonStream);
			return new ObjectMapper().readTree(parser);
		} catch (IOException e) {
			throw new GraphQLRestException(Status.BAD_REQUEST, "Error parsing JSON", e);
		}
	}

	@Override
	public <T> T read(Class<T> type, InputStream jsonStream) {
		JsonNode jsonNode = parseJson(jsonStream);
		try {
			return new ObjectMapper().readValue(new TreeTraversingParser(jsonNode), type);
		} catch (IOException e) {
			throw new GraphQLRestException(Status.BAD_REQUEST, "Error mapping JSON to object", e);
		}
	}

	@Override
	public void write(OutputStream out, JacksonWriterDelegate delegate) {
		// TODO: UTF-8 is hardcoded...
		try (JsonGenerator generator = sharedFactory.createGenerator(out, JsonEncoding.UTF8)) {
			delegate.write(generator);
		} catch (IOException e) {
			throw new GraphQLRestException(Status.INTERNAL_SERVER_ERROR, "Error writing JSON", e);
		}
	}

}
