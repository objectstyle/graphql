package org.objectstyle.cayenne.graphql.jaxrs;

/**
 * A value holder object for GraphQL queries coming from REST API.
 */
public class GraphQLRestQuery {

	private String query;
	private String variables;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getVariables() {
		return variables;
	}
	
	public void setVariables(String variables) {
		this.variables = variables;
	}
}
