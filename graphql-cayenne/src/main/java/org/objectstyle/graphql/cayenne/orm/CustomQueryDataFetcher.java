package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Select;
import org.apache.cayenne.query.SelectQuery;

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

        if (query instanceof SelectQuery) {
            Expression expression = ((SelectQuery) query).getQualifier();

            ((SelectQuery) query).setQualifier(expression.params(params));
        }

        if (query instanceof ObjectSelect) {
            Expression expression = ((ObjectSelect) query).getWhere();

            ((ObjectSelect) query).where(expression.params(params));
        }

        return query.select(objectContext);
    }
}
