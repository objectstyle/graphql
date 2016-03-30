package org.objectstyle.graphql.rest;

import javax.ws.rs.core.Response.Status;

/**
 * An exception thrown in REST layer, outside of GraphQL parsing and execution.
 */
public class BQGraphQLRestException extends RuntimeException {

	private static final long serialVersionUID = -1228276457093874409L;
	
	private Status status;

	public BQGraphQLRestException() {
		this(Status.INTERNAL_SERVER_ERROR);
	}

	public BQGraphQLRestException(Status status) {
		this(status, null, null);
	}

	public BQGraphQLRestException(Status status, String message) {
		this(status, message, null);
	}

	public BQGraphQLRestException(Status status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}
}
