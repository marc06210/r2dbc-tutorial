package com.mgu.r2dbc.web;

import com.mgu.r2dbc.repository.AirPlaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mgu.r2dbc.entity.AirPlane;

import reactor.core.publisher.Mono;

@Component
public class AirPlaneHandler {
    @Autowired
    protected AirPlaneRepository repository;

    // the following line requires ss4h-reactive
//    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ServerResponse> getAllAirPlanes(ServerRequest serverRequest) {
        Mono<ServerResponse> a = ServerResponse.ok()
                .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                .body(repository.findAll(), AirPlane.class);
        return a;
    }
}