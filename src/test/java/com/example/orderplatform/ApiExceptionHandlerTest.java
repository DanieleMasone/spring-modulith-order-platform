package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsUnexpectedFailuresToGenericProblemDetail() {
        var problem = handler.handleUnexpected(new IllegalStateException("internal detail"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Unexpected error");
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred.");
    }
}
