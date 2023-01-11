
This part follows the code demonstrated in the [introduction](01-r2dbc-introduction.md).

# The @Transaction annotation
Let's restart the application in order to start from a known database status.

We first check the airplanes **http :8080/airplanes**. We validate that we have three planes and all of them have
**workInProgress** to **false**.

We then create a route **echo -n '{"flightName": "AF003", "stationFrom": "NCE", "stationTo": "CDG"}' | http POST :8080/routes**.

We check that the route is created **http :8080/routes** will return one entry.

We check again the airplanes **http :8080/airplanes**. We validate that we have three planes and all of them have **workInProgress**
to **false**.

Now we will create the same route again but for flight AF002 **echo -n '{"flightName": "AF002", "stationFrom": "NCE", "stationTo": "CDG"}' | http POST :8080/routes**.
The return code of that invocation is **520** which is expected because it corresponds to the error handler we have defined and
an exception has been raised due to the violation of the SQL constraint.

So let's check again the airplanes... but wait this time the flight AF002 has still the **workInProgress** set to **true**!!!
That is not normal, there is then no transaction. Well, in fact, that was expected because we have not indicated at any point
that we wanted our service to be transactional.

So let's add the **@Transactional** annotation to the **createRoute()** method of the **ProgramService** class.

Restart the application and run the same scenario. In that configuration, after having raised the SQL exception we can
check that the AF002 has its **workInProgress** attribute set to **false**. So all the process was in a transaction and
the exception rollbacked everything.

# A step further

We have seen how to use the **@Transactional** annotation to indicate that process is transactional. Using this annotation
we have some limitations, the annotated methods must be public and invoked externally (ie. the transactional method must 
be invoked by another class that the service because the transaction is handled in an AOP way by Spring). Moreover, all 
transactions will then share the same settings.

Sometimes we want finer control of the transaction for a particular method, or we want to create a transaction for an 
internal call. It is then possible to inject manually a TransactionalOperator 
(https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/reactive/TransactionalOperator.html and https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-prog-operator) into our functional flow.

Here is the modified version of the **ProgramService** class that uses this approach.

    package com.mgu.r2dbc.service;

    import java.util.Arrays;
    import java.util.List;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.reactive.TransactionalOperator;
    
    import com.mgu.r2dbc.entity.AirPlane;
    import com.mgu.r2dbc.entity.FlightRoute;
    import com.mgu.r2dbc.entity.Station;
    import com.mgu.r2dbc.repository.AirPlaneRepository;
    import com.mgu.r2dbc.repository.FlightRouteRepository;
    import com.mgu.r2dbc.repository.StationRepository;
    import com.mgu.r2dbc.web.request.CreateRouteInput;
    
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;
    import reactor.util.function.Tuples;
    
    @Service
    public class ProgramService {
        @Autowired
        private StationRepository stationRepository;
        @Autowired
        private AirPlaneRepository airPlaneRepository;
        @Autowired
        private FlightRouteRepository flightRouteRepository;
    
        @Autowired
        private TransactionalOperator operator;
     
        public Mono<Void> createRoute(CreateRouteInput input) {
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
                    .as(operator::transactional) // alternate solution for the transaction
                    .then();
        }
     
        protected Station getStationByCode(String iataCode, List<Station> stations) {
            return stations.stream().filter(station -> iataCode.equals(station.getIataCode())).findFirst().orElse(null);
        }
    }

## Next steps

[R2DBC and Unit testing](03-r2dbc-junit.md)

[R2DBC and relations](04-r2dbc-relations.md)

[R2DBC and complex mappings](05-r2dbc-complex-mappings.md)

[R2DBC and queries](06-r2dbc-queries.md)
