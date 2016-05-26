package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.Select;

public class CustomQueryDataFetcher implements DataFetcher {
    private ObjectContext objectContext;
    private Select<?> query;

    public CustomQueryDataFetcher(ObjectContext objectContext, Select<?> query) {
        this.objectContext = objectContext;
        this.query = query;
    }


    @Override
    public Object get(DataFetchingEnvironment environment) {
        return query.select(objectContext);
    }
}
