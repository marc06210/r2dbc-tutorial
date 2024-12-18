package com.mgu.r2dbc.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mgu.r2dbc.entity.FlightRoute;

import reactor.core.publisher.Flux;

public interface FlightRouteRepository extends ReactiveCrudRepository<FlightRoute, Long> {
    @Query("SELECT f.* FROM flight_route f, air_plane ap where ap.name = :flightName AND ap.id = f.flight_id")
    Flux<FlightRoute> findRoutesForFlight(String flightName);
}