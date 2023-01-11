For the **JUnit** part, let's focus on the **ProgramService** class.

We will create a unit test for that service.

Before that, let's add the reactive **h2** driver that we will use for our tests.

    <dependency>
        <groupId>io.r2dbc</groupId>
        <artifactId>r2dbc-h2</artifactId>
        <scope>test</scope>
    </dependency>

Create an **application.yml** file for the tests and configure the database connection.

    logging:
        level:
            root: INFO
    
    spring:
        r2dbc:
            url: r2dbc:h2:mem:///testdb

And now let's focus on the test. What we will do is inside the same method test a successful creation and also a failing one.

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


## Next steps

[R2DBC and relations](04-r2dbc-relations.md)

[R2DBC and complex mappings](05-r2dbc-complex-mappings.md)

[R2DBC and queries](06-r2dbc-queries.md)
