package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.*;
import org.apache.cayenne.reflect.ClassDescriptor;

import java.util.HashMap;
import java.util.Map;

public class CustomQueryDataFetcher implements DataFetcher {
    private ObjectContext objectContext;
    private Select<?> query;

    public CustomQueryDataFetcher(ObjectContext objectContext, Select<?> query) {
        this.objectContext = objectContext;
        this.query = query;
    }


    @Override
    public Object get(DataFetchingEnvironment environment) {

        Map<String, Object> params = new HashMap<>();

        environment.getArguments().forEach((k, v) -> {
            if (v != null) {
                params.put(k, v);
            }
        });

        // TODO: think about universality

        if (query instanceof ObjectSelect) {
            Expression expression = ((ObjectSelect) query).getWhere();

            QueryMetadata md = query.getMetaData(objectContext.getEntityResolver());

            ClassDescriptor cd = objectContext.getEntityResolver().getClassDescriptor(md.getObjEntity().getName());
            query = ObjectSelect.query(cd.getObjectClass()).where(expression.params(params));
        }

        if (query instanceof SQLSelect) {
            query = ((SQLSelect)query).params(params);
        }

        if (query instanceof SelectQuery) {
            query = ((SelectQuery)query).createQuery(params);
        }

        return query.select(objectContext);
    }
}
