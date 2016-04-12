package org.objectstyle.graphql.rest.provider;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.objectstyle.graphql.rest.GraphQLRestException;
import org.objectstyle.graphql.rest.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Singleton
public class GraphQLRestExceptionMapper implements ExceptionMapper<GraphQLRestException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLRestExceptionMapper.class);

    @Override
    public Response toResponse(GraphQLRestException exception) {

        String message = exception.getMessage();
        Status status = exception.getStatus();

        if (LOGGER.isInfoEnabled()) {
            StringBuilder log = new StringBuilder();
            log.append(status.getStatusCode()).append(" ").append(status.getReasonPhrase());

            if (message != null) {
                log.append(" (").append(message).append(")");
            }

            if (exception.getCause() != null && exception.getCause().getMessage() != null) {
                log.append(" [cause: ").append(exception.getCause().getMessage()).append("]");
            }

            // include stack trace in debug mode...
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(log.toString(), exception);
            } else {
                LOGGER.info(log.toString());
            }
        }

        return Response.status(status).entity(new MessageResponse(message)).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
