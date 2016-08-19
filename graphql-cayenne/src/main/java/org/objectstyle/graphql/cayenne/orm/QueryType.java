package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.DataFetcher;
import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.Select;

import java.util.HashMap;
import java.util.Map;

public class QueryType extends BaseType {
    private ObjectContext objectContext;
    private Map<String, Class<? extends DataFetcher>> dataFetchers = new HashMap<>();
    private Map<String, Select<?>> queries = new HashMap<>();

    public Map<String, Select<?>> getQueries() {
        return queries;
    }

    public Map<String, Class<? extends DataFetcher>> getDataFetchers() {
        return dataFetchers;
    }

    public static QueryType.Builder builder(ObjectContext objectContext) {
        return new QueryType.Builder(objectContext);
    }

    public static class Builder extends BaseType.Builder {
        private QueryType queryType;

        private Builder(ObjectContext objectContext) {
            super(objectContext);

            queryType = new QueryType();
            queryType.objectContext = objectContext;
        }

        private ObjEntity getObjEntityByClass(Class<? extends CayenneDataObject> entity) {
            return this.queryType.objectContext.getEntityResolver().getObjEntity(((Class) entity).getSimpleName());
        }

        public Builder query(String propertyName, Select<?> query) {
            queryType.queries.put(propertyName, query);
            return this;
        }

        public Builder dataFetcher(String entity, Class<? extends DataFetcher> dataFetcher) {
            queryType.dataFetchers.put(entity, dataFetcher);
            return this;
        }

        public Builder dataFetcher(Class<? extends CayenneDataObject> entity, Class<? extends DataFetcher> dataFetcher) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                return dataFetcher(oe.getName(), dataFetcher);
            }
            return this;
        }

        public QueryType build() {
            queryType.setEntityBuilder(super.build().getEntityBuilder());
            return queryType;
        }
    }
}
