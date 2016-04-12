package org.objectstyle.graphql.rest;

import javax.ws.rs.core.Response.Status;

/**
 * An exception thrown in REST layer, outside of GraphQL parsing and execution.
 */
public class GraphQLRestException extends RuntimeException {

    private static final long serialVersionUID = -1228276457093874409L;

    private Status status;

    public GraphQLRestException() {
        this(Status.INTERNAL_SERVER_ERROR);
    }

    public GraphQLRestException(Status status) {
        this(status, null, null);
    }

    public GraphQLRestException(Status status, String message) {
        this(status, message, null);
    }

    public GraphQLRestException(Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
