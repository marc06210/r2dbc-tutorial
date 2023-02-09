package com.mgu.r2dbc.web;

import com.mgu.r2dbc.entity.Station;
import com.mgu.r2dbc.repository.AirPlaneRepository;
import com.mgu.r2dbc.repository.FlightRouteRepository;
import com.mgu.r2dbc.repository.StationRepository;
import com.mgu.r2dbc.service.ProgramService;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

/**
 * Here an alternative WebClientTest configuration, we bind it to the router configuration.
 * It also works to bind it to the ApplicationContext.<br/>
 * Annotation WebFluxTest disables full auto-configuration, it only applies configuration
 * relevant to WebFlux tests (Controller, ControllerAdvice, JsonComponent, Converter) but
 * nothing else.<br/>
 * As auto-configuration is disabled, we have to define what will serve to configure
 * our ApplicationContext (here Router and Handler).<br/>
 * This approach clearly shows that the granularity is not good in our router
 * configuration, ie. in order to test endpoints dedicated to specific biz, we have
 * to define almost everything.<br/>
 * The profile 'optim' enables a better configuration (not perfect) and the
 * ApplicationRouterWebFluxTest2 shows the impact on the test class which is lighter than
 * this one.
 */
@WebFluxTest
@ContextConfiguration
        (classes = { ApplicationRouter.class, AirPlaneHandler.class, ProgramHandler.class, StationHandler.class })
class ApplicationRouterWebFluxTest {

    private WebTestClient webTestClient;

    @MockBean
    private StationRepository stationRepository;
    @MockBean
    private FlightRouteRepository flightRouteRepository;
    @MockBean
    private AirPlaneRepository airplaneRepository;

    @MockBean
    private ConnectionFactory connectionFactory;
    @MockBean
    private ProgramService programService;

    @Autowired
    private ApplicationRouter router;
    @Autowired
    private StationHandler sh;

    @BeforeEach
    public void initWebTestClient() {
        webTestClient = WebTestClient.bindToRouterFunction(router.stationRouter(sh)).build();
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