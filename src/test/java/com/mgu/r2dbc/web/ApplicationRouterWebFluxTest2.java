package com.mgu.r2dbc.web;

import com.mgu.r2dbc.entity.Station;
import com.mgu.r2dbc.repository.StationRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;


@WebFluxTest
@ContextConfiguration(classes = { StationHandler.class })
@ActiveProfiles("optim")
class ApplicationRouterWebFluxTest2 {

    private WebTestClient webTestClient;

    @MockBean
    private StationRepository stationRepository;
    @MockBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private StationHandler sh;

    @BeforeEach
    public void initWebTestClient() {
        webTestClient = WebTestClient.bindToRouterFunction(sh.stationRouter()).build();
    }

    @Test
    @DisplayName("Test stations endpoint")
    void testStationsEndpoint() {
        when(stationRepository.findAll()).thenReturn(Flux.just(new Station("1", "S1"),
                new Station("2", "S2"),
                new Station("2", "S3")));
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