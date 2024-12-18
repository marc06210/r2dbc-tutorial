package com.mgu.r2dbc.service;

import java.util.Arrays;
import java.util.List;

import com.mgu.r2dbc.entity.AirPlane;
import com.mgu.r2dbc.entity.FlightRoute;
import com.mgu.r2dbc.entity.Station;
import com.mgu.r2dbc.repository.AirPlaneRepository;
import com.mgu.r2dbc.repository.FlightRouteRepository;
import com.mgu.r2dbc.repository.StationRepository;
import com.mgu.r2dbc.web.request.CreateRouteInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
public class ProgramService {
    private final StationRepository stationRepository;
    private final AirPlaneRepository airPlaneRepository;
    private final FlightRouteRepository flightRouteRepository;

    public ProgramService(StationRepository stationRepository, AirPlaneRepository airPlaneRepository, FlightRouteRepository flightRouteRepository) {
        this.stationRepository = stationRepository;
        this.airPlaneRepository = airPlaneRepository;
        this.flightRouteRepository = flightRouteRepository;
    }

    @Transactional
    public Mono<Boolean> createRoute(CreateRouteInput input) {
        Flux<Station> stations = stationRepository
                .findByIataCodeIn(Arrays.asList(input.stationFrom(), input.stationTo()));
        Mono<AirPlane> airPlane = airPlaneRepository.findByName(input.flightName())
                .doOnNext(ap -> ap.setWorkInProgress(true)).flatMap(airPlaneRepository::save);

        return Mono
                .zip(airPlane, stations.collectList(),
                        (airplane, st) -> Tuples.of(airPlane,
                                new FlightRoute(getStationByCode(input.stationFrom(), st).getId(),
                                        getStationByCode(input.stationTo(), st).getId(), airplane.getId())))
                .flatMap(t -> flightRouteRepository.save(t.getT2()).thenReturn(t.getT1())).flatMap(ap -> ap)
                .doOnNext(ap -> ap.setWorkInProgress(false))
                .flatMap(airPlaneRepository::save)
                .thenReturn(Boolean.TRUE);
    }

    protected Station getStationByCode(String iataCode, List<Station> stations) {
        return stations.stream()
                .filter(station -> iataCode.equals(station.getIataCode()))
                .findFirst()
                .orElse(null);
    }

    public Flux<FlightRoute> loadAllRoutesAndRelations() {
        return flightRouteRepository.findAll()
                .flatMap(this::loadRouteDependencies);
    }

    private Mono<FlightRoute> loadRouteDependencies(final FlightRoute item) {
        Mono<FlightRoute> flightRouteWithAirPlane = Mono.just(item)
                .zipWith(airPlaneRepository.findById(item.getFlightId()))
                .map(result -> {
                    result.getT1().setFlight(result.getT2());
                    return result.getT1();
                });
        return stationRepository.findAllById(Flux.fromIterable(List.of(item.getFromStation(), item.getToStation())))
                .doOnNext(s -> {
                    if (s.getId().equals(item.getFromStation())) {
                        item.setStationFrom(s);
                    } else {
                        item.setStationTo(s);
                    }
                })
                .then(flightRouteWithAirPlane);
    }
}