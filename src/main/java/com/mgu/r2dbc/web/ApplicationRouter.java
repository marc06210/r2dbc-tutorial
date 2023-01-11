package com.mgu.r2dbc.web;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import com.mgu.r2dbc.entity.AirPlane;
import com.mgu.r2dbc.entity.FlightRoute;
import com.mgu.r2dbc.entity.Station;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Component in charge of registering all the application endpoint routes.
 */
@Component
public class ApplicationRouter {

    /**
     * Routes dedicated to {@link Station}
     * @param sh - the StationHandler bean
     * @return
     */
    @Bean
    RouterFunction<ServerResponse> stationRouter(StationHandler sh) {
        return RouterFunctions
                .route(GET("/stations"), sh::getAllStationsWithRepositoryQuery)
                .andRoute(GET("/stations-v2"), sh::getAllStationsWithTemplates)
                .andRoute(GET("/stations-v3"), sh::getAllStationsWithExampleMatcher)
                .andRoute(GET("/stations/{iataCode}"), sh::getStationByIataCode)
                .andRoute(POST("/stations").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), sh::createStation);
    }

    /**
     * Routes dedicated to {@link AirPlane}
     * @param aph - the AirPlaneHandler bean
     * @return
     */
    @Bean
    RouterFunction<ServerResponse> airplaneRouter(AirPlaneHandler aph) {
        return RouterFunctions
                .route(GET("/airplanes"), aph::getAllAirPlanes)
                ;
    }

    /**
     * Routes dedicated to {@link FlightRoute}
     * @param ph - the ProgramHandler bean
     * @return
     */
    @Bean RouterFunction<ServerResponse> programRouter(ProgramHandler ph) {
        return RouterFunctions
                .route(GET("/routes"), ph::getAllRoutes)
                .andRoute(GET("/airplanes/{flightName}/routes"), ph::getRouteForFlight)
                .andRoute(POST("/routes").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), ph::createRoute);
    }
}
