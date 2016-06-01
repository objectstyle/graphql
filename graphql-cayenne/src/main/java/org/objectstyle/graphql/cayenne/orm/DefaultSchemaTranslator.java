package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.ObjectContext;

import graphql.schema.GraphQLSchema;

public class DefaultSchemaTranslator implements SchemaTranslator {

    private ObjectContext selectContext;

    public DefaultSchemaTranslator(ObjectContext selectContext) {
        this.selectContext = selectContext;
    }

    @Override
    public GraphQLSchema toGraphQL() {
        return SchemaBuilder.builder(EntityBuilder.builder(selectContext).build())
                .build();
    }
}
