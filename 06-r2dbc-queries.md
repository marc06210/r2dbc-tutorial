It exists many ways to perform queries.

On this page, we will see how to retrieve all the stations where the full name starts with a certain value 
(whatever case it is). From a SQL point of view, the request we want to perform is then: 
**WHERE (UPPER(station.full_name) LIKE UPPER($1)**



# With the Repository
## Using Queries defined in the Repository
For a full list of query options, please have a look at the official documentation: https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#r2dbc.repositories.queries

To perform our request we have to define the query like this in the **StationRepository** class.

```java
package com.mgu.r2dbc.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mgu.r2dbc.entity.Station;

import reactor.core.publisher.Flux;

public interface StationRepository extends ReactiveCrudRepository<Station, Long> {
    public Flux<Station> findByFullNameLikeIgnoreCase(String fullName);
}
```

The invocation from the request handler is straightforward in that case.

```java
public Mono<ServerResponse> getAllStationsWithRepositoryQuery(ServerRequest serverRequest) {
    Flux<Station> data = serverRequest.queryParam("fullName")
        .map(s -> repository.findByFullNameLikeIgnoreCase(s+"%"))
        .orElse(repository.findAll());

    return ServerResponse.ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(data, Station.class);
}
```

The code might look complicated, but it is only to handle the injection of the search criteria. Indeed, we can invoke 
the endpoint with **:8080/stations** or with **:8080/stations?fullName=paris**

## Using QueryByExample
It is possible to perform the queries using the Example approach (like in a standard JPA way). To use this approach, 
we have to enrich our **Repository** by extending the **ReactiveQueryByExampleExecutor<T>** interface.

```java
package com.mgu.r2dbc.repository;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mgu.r2dbc.entity.Station;

public interface StationRepository extends ReactiveCrudRepository<Station, Long>, ReactiveQueryByExampleExecutor<Station> {
}
```

The complexity of the request is then moved into the handler. The writing is more complex but, we gain flexibility 
when creating the request that matches our input parameters (well not in our example but let's consider a more complex
model, you can add criteria programmatically and then cover a wide range of possible requests from the same code).

```java
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
```

# Without Repository

Another approach is to use **R2dbcEntityTemplate**. In this situation, we do not need **Repository** anymore. So everything is 
done in the handler and, we need to inject the **ConnectionFactory** in the handler.

Methods for the criteria class are available [here](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#r2dbc.datbaseclient.fluent-api.criteria).

```java
package com.mgu.r2dbc.web;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.mgu.r2dbc.entity.Station;

import reactor.core.publisher.Flux;
import static org.springframework.data.relational.core.query.Criteria.where;

@Component
public class StationHandler {
    @Autowired
    private ConnectionFactory connectionFactory;

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
}
```
