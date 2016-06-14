package org.objectstyle.graphql.cayenne.orm;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;

import graphql.schema.GraphQLList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class DefaultDataFetcher implements DataFetcher {

	private ObjectContext objectContext;

	public DefaultDataFetcher(ObjectContext objectContext) {
		this.objectContext = objectContext;
	}

	@Override
	public Object get(DataFetchingEnvironment environment) {
		DefaultFilters df = new DefaultFilters();
		Map<String, Object> params = new HashMap<>();
		Map<FilterType, Object> filters = new HashMap<>();

        Object type = environment.getFieldType();
        String entityName;
        if(type instanceof GraphQLList){
            entityName = ((GraphQLList) type).getWrappedType().getName();
        } else {
            entityName = environment.getFieldType().getName();
        }

        environment.getArguments().forEach((k, v) -> {
			if(v != null) {
				if(df.isFilterName(k)) {
					filters.put(df.getFilterType(k), v);
				} else {
					params.put(k, v);
				}
			}
		});
		
		if(environment.getSource() != null) {
			Object obj = ((DataObject)environment.getSource()).readProperty(environment.getFields().get(0).getName());
					
			if(params.isEmpty() && filters.isEmpty())
				return obj;
	        
			if((obj instanceof List) && ((List<?>)obj).size() > 1) {
				applyFilters(filters, (List<?>)obj);
				
				if(params.isEmpty())
					return obj;
			}
			
			Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
			return expression.filterObjects((List<?>)obj);
		}

        Expression expression = ExpressionFactory.matchAllExp(params, Expression.EQUAL_TO);
        SelectQuery<?> query = new SelectQuery<>(entityName, expression);
			
        applyFilters(filters, query);
        
		return query.select(objectContext);
	}
	
	private void applyFilters(Map<FilterType, Object> filters, List<?> obj){
		applyFilters(filters, null, obj);
	}
	
	private void applyFilters(Map<FilterType, Object> filters, SelectQuery<?> query){
		applyFilters(filters, query, null);
	}
	
	private void applyFilters(Map<FilterType, Object> filters, SelectQuery<?> query, List<?> obj){
        filters.forEach((k, v) -> {
			switch(k) {
				case FIRST:
					if(query != null) {
						query.setFetchOffset(0);
						
						for(Object o : (List<?>) v) {
							query.setFetchLimit(Integer.parseInt(o.toString()));
							break;
						}
					}
					
					if(obj != null) {
						for(Object o : (List<?>) v) {
							Integer index = Integer.parseInt(o.toString());
							if(index < obj.size()) {
								obj.subList(index, obj.size()).clear();
							}
							
							break;
						}
					}
					
					break;
				case ASCENDING:
				case DESCENDING:	
					if(query != null) {
						for(Object o : (List<?>) v) {
							query.addOrdering(o.toString(), k == FilterType.ASCENDING ? SortOrder.ASCENDING : SortOrder.DESCENDING);					
						}
					}
					
					if(obj != null) {
						for(Object o : (List<?>) v) {
							Ordering order = new Ordering(o.toString(), k == FilterType.ASCENDING ? SortOrder.ASCENDING : SortOrder.DESCENDING);
							order.orderList(obj);
						}			
					}
					
					break;
				default:
					break;
			}
		});
	}
	
}
