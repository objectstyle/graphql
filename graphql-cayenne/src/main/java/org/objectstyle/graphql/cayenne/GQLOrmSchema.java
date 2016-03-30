package org.objectstyle.graphql.cayenne;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphql.schema.GraphQLSchema;

/**
 * A meta-schema that describes how to build {@link GraphQLSchema} out of an ORM
 * model. Provides a builder API.
 * 
 * @see GQLOrmEntitySchema
 */
public class GQLOrmSchema {

	public static Builder newBuilder() {
		return new Builder();
	}

	private Map<String, GQLOrmEntitySchema> entities;

	private GQLOrmSchema() {
		this.entities = new HashMap<>();
	}

	public Set<GQLOrmEntitySchema> getEntities() {
		return new HashSet<>(entities.values());
	}

	public static class Builder {

		private GQLOrmSchema schema;

		private Builder() {
			this.schema = new GQLOrmSchema();
		}

		/**
		 * Merges provided entity schemas into the schema builder.
		 * 
		 * @param entitySchemas
		 *            an array of entity schemas.
		 * @return this builder
		 */
		public Builder merge(GQLOrmEntitySchema... entitySchemas) {
			return this;
		}

		/**
		 * Adds entity schemas into the builder, replacing any previously
		 * created schemas for matching entities.
		 * 
		 * @param entitySchemas
		 *            an array of entity schemas.
		 * @return this builder
		 */
		public Builder replace(GQLOrmEntitySchema... entitySchemas) {
			return this;
		}

		public GQLOrmSchema build() {
			return schema;
		}
	}
}
