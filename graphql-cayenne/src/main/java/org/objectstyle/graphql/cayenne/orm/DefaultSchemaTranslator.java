package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;

import graphql.schema.GraphQLSchema;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultSchemaTranslator implements SchemaTranslator {

    private ObjectContext selectContext;

    public DefaultSchemaTranslator(ObjectContext selectContext) {
        this.selectContext = selectContext;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public GraphQLSchema toGraphQL(EntityResolver cayenneSchema) {

        SchemaBuilder schemaBuilder = SchemaBuilder.newSchemaBuilder()
                .objectContext(selectContext)
                .entityResolver(cayenneSchema)
                .build();

        return schemaBuilder.getGraphQLSchema();
    }
}
