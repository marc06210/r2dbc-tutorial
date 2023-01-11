package com.mgu.r2dbc.service;

import com.mgu.r2dbc.web.request.CreateRouteInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class ProgramServiceTest {
    @Autowired
    protected ProgramService service;

    @Test
    @DisplayName("Test CreateRoute Service")
    void testCreateRouteService() {
        Mono<Boolean> monoCreateRoute = service.createRoute(new CreateRouteInput("AF001", "CDG", "NCE"));
        StepVerifier.create(monoCreateRoute)
                .expectNext(Boolean.TRUE)
                .verifyComplete();
        StepVerifier.create(monoCreateRoute)
                .expectError()
                .verify();
    }
}