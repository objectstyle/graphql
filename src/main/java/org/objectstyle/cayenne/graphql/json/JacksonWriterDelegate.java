package org.objectstyle.cayenne.graphql.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public interface JacksonWriterDelegate {

	void write(JsonGenerator generator) throws IOException;
}
