package org.objectstyle.cayenne.graphql.json;

import java.io.OutputStream;

public interface JsonWriter {

	void write(OutputStream out, JacksonWriterDelegate delegate);
}
