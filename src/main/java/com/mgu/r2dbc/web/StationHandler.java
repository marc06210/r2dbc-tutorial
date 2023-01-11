package com.mgu.r2dbc.web;

import com.mgu.r2dbc.repository.StationRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mgu.r2dbc.entity.Station;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * Contain all request handlers dealing with {@link Station} entity/table.
 */
@Component
public class StationHandler {
    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    protected StationRepository repository;

    /**
     * Return a list of {@link Station} making the request with the {@link R2dbcEntityTemplate}.
     * <p>If the request has <i>fullName</i> query parameter then return
     * a list of {@link Station} where <i>fullName</i> is like the parameter, otherwise
     * return all {@link Station}</p>
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> getAllStationsWithTemplates(ServerRequest serverRequest) {
        R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);

        Flux<Station> data = serverRequest.queryParam("fullName")
                .map(s -> s + '%')
                .map(s -> template.select(Station.class).matching(Query.query(where("fullName").like(s).ignoreCase(true))).all())
                .orElse(template.select(Station.class).all());

        return ServerResponse.ok()
                .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                .body(data, Station.class);
    }

    /**
     * Return a list of {@link Station}.
     * <p>If the request has <i>fullName</i> query parameter then return
     * a list of {@link Station} where <i>fullName</i> is like the parameter, otherwise
     * return all {@link Station}</p>
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> getAllStationsWithExampleMatcher(ServerRequest serverRequest) {
        Flux<Station> data = serverRequest.queryParam("fullName")
                .map(s -> {
                    ExampleMatcher matcher = ExampleMatcher.matching()
                            .withMatcher("fullName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
                    Example<Station> example = Example.of(new Station(null, s), matcher);
                    return repository.findAll(example);
                })
                .orElse(repository.findAll());

        return ServerResponse.ok()
                .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                .body(data, Station.class);
    }

    /**
     * Return a list of {@link Station} using queries defined in the Repository interface.
     * <p>If the request has <i>fullName</i> query parameter then return
     * a list of {@link Station} where <i>fullName</i> is like the parameter, otherwise
     * return all {@link Station}</p>
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> getAllStationsWithRepositoryQuery(ServerRequest serverRequest) {
        Flux<Station> data = serverRequest.queryParam("fullName")
                .map(s -> repository.findByFullNameLikeIgnoreCase(s+"%"))
                .orElse(repository.findAll());

        return ServerResponse.ok()
                .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                .body(data, Station.class);
    }

    /**
     * Get a {@link Station} by its IATA code
     * @param request
     * @return
     */
    public Mono<ServerResponse> getStationByIataCode(ServerRequest request) {
        return repository.findByIataCode(request.pathVariable("iataCode"))
                .flatMap(s -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(s), Station.class))
                .switchIfEmpty(ServerResponse.notFound().build())
                ;
    }

    /**
     * Create a {@link Station}
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> createStation(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .body(repository.saveAll(serverRequest.bodyToMono(Station.class)), Station.class)
                ;
    }
}
