package com.mgu.r2dbc.repository;

import java.util.Collection;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mgu.r2dbc.entity.Station;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StationRepository extends ReactiveCrudRepository<Station, Long>, ReactiveQueryByExampleExecutor<Station> {
    Mono<Station> findByIataCode(String iataCode);
    Flux<Station> findByIataCodeIn(Collection<String>iataCodes);
    Flux<Station> findByFullNameLikeIgnoreCase(String fullName);
}