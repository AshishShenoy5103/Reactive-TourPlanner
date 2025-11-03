package com.tourplanner.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        if(ex instanceof AdminAccessDeniedException) {
            return List.of(GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(ErrorType.FORBIDDEN)
                    .build());
        } else if(ex instanceof BookingIdNotFoundException || ex instanceof UserNotFoundException) {
            return List.of(GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(ErrorType.NOT_FOUND)
                    .build());
        } else if(ex instanceof InvalidCredentialsException || ex instanceof UnauthorizedAccessException) {
            return List.of(GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(ErrorType.UNAUTHORIZED)
                    .build());
        } else {
            return List.of(GraphqlErrorBuilder.newError(env)
                    .message("Unexpected error: " + ex.getMessage())
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .build());
        }
    }
}

