package org.objectstyle.cayenne.graphql.orm;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

class DefaultDataFetcher implements DataFetcher {

	private String entity;
	private ObjectContext context;
	private String relationship = null;

	public DefaultDataFetcher(String entity, ObjectContext context) {
		this.entity = entity;
		this.context = context;
	}
	
	public DefaultDataFetcher(String entity, ObjectContext context, String relationship) {
		this.entity = entity;
		this.context = context;
		this.relationship = relationship;
	}

	@Override
	public Object get(DataFetchingEnvironment environment) {
		DefaultFilters df = new DefaultFilters();
		Map<String, Object> params = new HashMap<String, Object>();
		Map<FilterType, Object> filters = new HashMap<FilterType, Object>();
		
		environment.getArguments().forEach((k, v) -> {
			if(v != null) {
				if(df.isFilterName(k)) {
					filters.put(df.getFilterType(k), v);
				} else {
					params.put(k, v);
				}
			}
		});
		
		if(relationship != null) {
			Object obj = ((DataObject)environment.getSource()).readProperty(relationship);
					
			if(params.isEmpty())
				return obj;
			
			Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
			return expression.filterObjects((List<?>)obj);
		}
			
        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<Object> query = new SelectQuery<>(entity, expression);
			
        filters.forEach((k, v) -> {
			switch(k) {
				case FIRST:
					query.setFetchOffset(0);
					
					for(Object o : (List<?>) v) {
						query.setFetchLimit(Integer.parseInt(o.toString()));
						break;
					}
	
					break;
				case ASCENDING:
				case DESCENDING:						
					for(Object o : (List<?>) v) {
						query.addOrdering(o.toString(), k == FilterType.ASCENDING ? SortOrder.ASCENDING : SortOrder.DESCENDING);					
					}
					
					break;
				default:
					break;
			}
		});
        
		return query.select(context);
	}
	
}
