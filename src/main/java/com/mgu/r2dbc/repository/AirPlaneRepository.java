package com.mgu.r2dbc.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mgu.r2dbc.entity.AirPlane;

import reactor.core.publisher.Mono;

public interface AirPlaneRepository extends ReactiveCrudRepository<AirPlane, Long> {
    Mono<AirPlane> findByName(String name);
}