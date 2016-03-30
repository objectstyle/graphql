package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.map.EntityResolver;

import graphql.schema.GraphQLSchema;

public interface SchemaTranslator {

	GraphQLSchema toGraphQL(EntityResolver cayenneSchema);
}
