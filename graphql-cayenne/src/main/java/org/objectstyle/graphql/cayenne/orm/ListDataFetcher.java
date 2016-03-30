package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.SelectQuery;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class ListDataFetcher implements DataFetcher {

	private String entity;
	private ObjectContext context;

	public ListDataFetcher(String entity, ObjectContext context) {
		this.entity = entity;
		this.context = context;
	}

	@Override
	public Object get(DataFetchingEnvironment env) {
		SelectQuery<?> query = new SelectQuery<>(entity);
		return query.select(context);
	}
}
