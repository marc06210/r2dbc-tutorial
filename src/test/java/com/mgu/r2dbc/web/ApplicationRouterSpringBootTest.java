package com.mgu.r2dbc.web;

import com.mgu.r2dbc.entity.Station;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * SpringBootTest sample. No difficulty, we only need to remember to bind
 * the WebTestClient to the ApplicationContext.
 */
@SpringBootTest
class ApplicationRouterSpringBootTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    public void initWebTestClient() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    @DisplayName("Test stations endpoint")
    void testStationsEndpoint() {
        webTestClient.get()
                .uri("/stations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Station.class)
                .value(stationsResponse -> {
                    Assertions.assertEquals(stationsResponse.size(), 3);
                    System.out.println(stationsResponse);
                });
    }
}