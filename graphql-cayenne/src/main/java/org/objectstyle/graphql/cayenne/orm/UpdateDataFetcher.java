package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.AttributeProperty;
import org.apache.cayenne.reflect.ClassDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateDataFetcher implements DataFetcher {

    private ObjectContext objectContext;

    public UpdateDataFetcher(ObjectContext objectContext) {
        this.objectContext = objectContext;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> arguments = environment.getArguments();

        Object type = environment.getFieldType();
        String entityName;
        if (type instanceof GraphQLList) {
            entityName = ((GraphQLList) type).getWrappedType().getName();
        } else {
            entityName = environment.getFieldType().getName();
        }

        ClassDescriptor cd = objectContext.getEntityResolver().getClassDescriptor(entityName);

        for(AttributeProperty ap : cd.getIdProperties()) {
            if(arguments.containsKey(ap.getName())) {
                params.put(ap.getName(), arguments.get(ap.getName()));
            }
        }

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>(entityName, expression);

        List<CayenneDataObject> obj = (List<CayenneDataObject>)query.select(objectContext);

        if(obj.size() > 1) {
            return obj;
        }

        if(obj.size() == 0) {
            try {
                CayenneDataObject cdo = (CayenneDataObject) cd.getObjectClass().newInstance();
                arguments.forEach((k, v) -> {
                    if (v != null) {
                        cdo.writeProperty(k, v);
                    }
                });

                objectContext.registerNewObject(cdo);
                obj.add(cdo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if(obj.size() == 1) {
            CayenneDataObject cdo = obj.get(0);

            arguments.forEach((k, v) -> {
                if (v != null) {
                    cdo.writeProperty(k, v);
                }
            });
        }

        objectContext.commitChanges();
        return obj;
    }
}
