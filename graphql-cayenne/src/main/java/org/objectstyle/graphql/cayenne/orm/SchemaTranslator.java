package org.objectstyle.graphql.cayenne.orm;

import graphql.schema.GraphQLSchema;

public interface SchemaTranslator {

	GraphQLSchema toGraphQL();
}
