package org.objectstyle.graphql.rest.json;

import java.io.InputStream;

public interface JsonReader {

    <T> T read(Class<T> type, InputStream jsonStream);
}
