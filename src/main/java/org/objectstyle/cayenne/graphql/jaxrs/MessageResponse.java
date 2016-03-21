package org.objectstyle.cayenne.graphql.jaxrs;

/**
 * A response object normally used for protocol errors outside of GraphQL
 * parsing and execution.
 */
// TODO: should we serve these as GraphQL responses as well?
public class MessageResponse {

	private String message;

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
