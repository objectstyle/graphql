package org.objectstyle.cayenne.graphql;

import java.util.Objects;

import graphql.schema.GraphQLSchema;

/**
 * A meta-schema that describes how to build {@link GraphQLSchema} parts related
 * to a single ORM model entity.
 * 
 * @see GQLOrmSchema
 */
public class GQLOrmEntitySchema {

	protected static final String FETCH_ALL_FORMAT = "%ss";
	protected static final String FETCH_BY_ID_FORMAT = "%sById";

	private Class<?> type;
	private String rootFetchField;
	private String rootFetchByIdField;

	public static <T> Builder<T> builder(Class<T> entityType) {
		return new Builder<>(entityType);
	}

	private GQLOrmEntitySchema(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean canFetch() {
		return rootFetchField != null;
	}

	public String getRootFetchField() {
		return rootFetchField;
	}
	
	public boolean canFetchById() {
		return rootFetchByIdField != null;
	}
	
	public String getRootFetchByIdField() {
		return rootFetchByIdField;
	}

	public static class Builder<T> {

		private GQLOrmEntitySchema schema;

		private Builder(Class<T> type) {
			Objects.requireNonNull(type);
			this.schema = new GQLOrmEntitySchema(type);
		}

		public Builder<T> canFetch() {
			String name = uncap(schema.getType().getSimpleName());
			String fieldName = String.format(FETCH_ALL_FORMAT, name);
			return canFetch(fieldName);
		}

		public Builder<T> canFetch(String fieldName) {
			Objects.requireNonNull(fieldName);
			schema.rootFetchField = fieldName;
			return this;
		}

		public Builder<T> canFetchById() {
			String name = uncap(schema.getType().getSimpleName());
			String fieldName = String.format(FETCH_BY_ID_FORMAT, name);
			return canFetchById(fieldName);
		}

		public Builder<T> canFetchById(String fieldName) {
			Objects.requireNonNull(fieldName);
			schema.rootFetchByIdField = fieldName;
			return this;
		}

		public GQLOrmEntitySchema build() {
			return schema;
		}

		protected String uncap(String entityName) {
			return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
		}
	}
}
