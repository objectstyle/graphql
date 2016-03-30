package org.objectstyle.graphql.rest.json;

import java.io.OutputStream;

public interface JsonWriter {

	void write(OutputStream out, JacksonWriterDelegate delegate);
}
