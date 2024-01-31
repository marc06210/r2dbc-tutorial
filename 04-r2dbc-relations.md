According to this [page](https://github.com/spring-projects/spring-data-r2dbc/issues/356), 
there should not be any native support of relations between R2DBC entities.

We will see on this page how to add such behavior to our application.

We will use the sample described in the [introduction](01-r2dbc-introduction.md) and, we will 
modify the **FlightRoute** entity in order to also contain links to the **Station** and 
**AirPlane** entities.

We will not update the database, ie. in the database the **flight_route** table will only hold
ids of records from the station table and from the **air_plane** table.

The first step is to update the **FlightRoute** entity in order to also contain two **Station**
objects and an **AirPlane** object. Those attributes should not be pushed into the database so,
we will annotate them with **@Transient**.

This gives us the new version of the **FlightRoute** class.
```java
package com.mgu.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Data
public class FlightRoute {
    @Id
    private Long id;
    private Long fromStation;
    private Long toStation;
    private Long flightId;

    @Transient
    private AirPlane flight;
    @Transient
    private Station stationFrom;
    @Transient
    private Station stationTo;
  
    public FlightRoute() {
    }
  
    public FlightRoute(long fromStation, long toStation, long flightId) {
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.flightId = flightId;
    }
}
```

Then we need to define a way to retrieve all the relations. We can't do it in the repositories, so we will use a service 
component to do it. In our project, we already have a service component **ProgramService** dedicated to the **FlightRoute**
object, we will then amend this class.

We will expose a method that retrieves all the records from the **flight_route** table and for each record, we will apply 
a function **loadRouteDependencies** whose role is to retrieve from the database both **Stations** and the **AirPlane**
to enrich the result.

```java
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
```

We can now modify our implementation in the **AirPlaneHandler** class to return the result of this new function 
**loadRoutesAndDependencies()** in place of the **findAll()** method.


**TODO:** check the use of bufferUntilChanged

## Next steps

[R2DBC and complex mappings](05-r2dbc-complex-mappings.md)

[R2DBC and queries](06-r2dbc-queries.md)
