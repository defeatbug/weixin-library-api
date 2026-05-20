package com.weixinlibrary.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handle(RuntimeException ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }
}
