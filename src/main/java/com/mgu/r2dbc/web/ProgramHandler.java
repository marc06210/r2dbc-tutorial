package com.mgu.r2dbc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mgu.r2dbc.entity.FlightRoute;
import com.mgu.r2dbc.repository.FlightRouteRepository;
import com.mgu.r2dbc.service.ProgramService;
import com.mgu.r2dbc.web.request.CreateRouteInput;

import reactor.core.publisher.Mono;

@Component
public class ProgramHandler {
    @Autowired
    protected ProgramService programService;
    @Autowired
    protected FlightRouteRepository flightRouteRepository;

    public Mono<ServerResponse> createRoute(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateRouteInput.class)
                .flatMap(programService::createRoute)
                .flatMap(b -> ServerResponse.ok().body(Mono.just(b.toString()), String.class))
                .onErrorResume(th -> ServerResponse.status(520).body(Mono.just(th.getMessage()), String.class))
        ;
    }

    public Mono<ServerResponse> getAllRoutes(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                .body(programService.loadAllRoutesAndRelations(), FlightRoute.class);
    }

    public Mono<ServerResponse> getRouteForFlight(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(flightRouteRepository.findRoutesForFlight(serverRequest.pathVariable("flightName")), FlightRoute.class);
    }
}