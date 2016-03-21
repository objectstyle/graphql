package org.objectstyle.cayenne.graphql.orm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class DefaultFilters {

	private Map<FilterType, String> filters;
	
	public DefaultFilters() {
		filters = new HashMap<FilterType, String>();
		
		for(FilterType ft : FilterType.values()){
			filters.put(ft, "_" + ft.toString().toLowerCase());
		}
	}
	
	public Map<FilterType, String> getFilters() {
		return filters;
	}
	
	public Collection<String> getFiltersNames(){
		return filters.values();
	}
	
	public Boolean isFilterName(String filter) {
		return filters.containsValue(filter);
	}
	
	public FilterType getFilterType(String filter){
		for(FilterType k: filters.keySet()) {
		    if(filters.get(k).equals(filter)) {
		        return k;  
		    }
		}
		
		return FilterType.UNDEFINED;
	}
}
