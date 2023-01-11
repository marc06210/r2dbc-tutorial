Created in March 2022.

This page is a short introduction to the R2DBC feature. R2DBC is the reactive way of doing JDBC.

The Spring documentation about R2DBC is available [here](https://docs.spring.io/spring-data/r2dbc/docs/1.1.0.RELEASE/reference/html/#get-started:first-steps:what).

The code within this repository contains the update covered in the [second part](02-r2dbc-relations.md) of the article.

This tutorial duplicates the code that is also present in the sources but the sources hold the final code.
If you follow the tutorial you will see the evolution of the code.

# Project creation

Let's create the project with the **Spring initializer**.

In the dependencies let's add the **Spring Reactive Web** and **Spring Data R2DBC**. 
We also add the **Postgresql** driver (but make sure to take the one providing the Reactive support).
As we are in a demo project, we can include **lombok**.

In the **pom.xml** this will also include the **org.postgresql:postgresql** library. This library is 
the JDBC version of the driver, we can remove it from the **pom.xml**.

# Database creation

With R2DBC it is not possible to create the database structure from the entities. We need to provide a SQL 
file that we will have to load at startup.

Here is the content of the **createDatabase.sql** file

    DROP TABLE IF EXISTS flight_route;
    DROP TABLE IF EXISTS station;
    DROP TABLE IF EXISTS air_plane;
    DROP TABLE IF EXISTS schedule;
    
    CREATE TABLE station (id SERIAL PRIMARY KEY, iata_code VARCHAR(3) NOT NULL UNIQUE, full_name VARCHAR(255));
    
    CREATE TABLE flight_route (id SERIAL PRIMARY KEY, from_station INT, to_station INT,
    flight_id INT, UNIQUE(from_station, to_station));
    
    CREATE TABLE air_plane (id SERIAL PRIMARY KEY, name VARCHAR(7) NOT NULL, work_in_progress BOOLEAN DEFAULT FALSE);
    
    CREATE TABLE schedule (id SERIAL PRIMARY KEY, id_route INT NOT NULL, id_plane INT NOT NULL);
    
    insert into station(iata_code, full_name) values ('CDG', 'Paris Charles de Gaulle'),
    ('ORY', 'Paris Orly'),('NCE', 'Nice Cote d Azur');
    
    insert into air_plane(name) values ('AF001'),('AF002'),('AF003');

The **air_plane** table contains a **work_in_progress** column that we will use to demonstrate the transactions.



You can either run this script manually or here we will load it at startup. As we drop and create everything, it means that any time we start the application we will be in the same situation.

To do it we define a **ConnectionFactoryInitializer** bean that can populate a database.

We do it in our main class.

    package com.mgu.r2dbc;
    
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.annotation.Bean;
    import org.springframework.core.io.ClassPathResource;
    import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
    import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
    
    import io.r2dbc.spi.ConnectionFactory;
    
    @SpringBootApplication
    public class R2dbcTutorialApplication {
        public static void main(String[] args) {
            SpringApplication.run(R2dbcTutorialApplication.class, args);
        }
    
        @Bean
        public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
            ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
            initializer.setConnectionFactory(connectionFactory);
            initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("createDatabase.sql")));
            return initializer;
        }
    }

And of course, we configure the **application.yml** file with the database connection information. And we will also
set the log level for database queries to **DEBUG**

    logging:
      level:
        root: INFO
        '[io.r2dbc.postgresql.QUERY]': DEBUG # log the queries
    spring:
      r2dbc:
        url: r2dbc:postgresql://localhost:5432/mgu_reactive
        username: mguuser
        password: mgupassword

**NB:** if we want to also log the parameters injected in the request, we can set the log level of **io.r2dbc.postgresql.PARAM** 
to **DEBUG**.

Now let's start the application. We will see in the logs all the SQL requests from our **createDatabase.sql** file, and we can also connect to the database and check that we have the tables and data.

# The R2DBC code
Now that our database is initialized then we can focus on our entities, repositories, and services.

## The entities

For the entities, we do not need any special annotation apart from the id attribute. Unlike the standard JPA part, 
this annotation is not part of the **javax.persistence** package but comes from the **org.springframework.data.annotation** package.

Unlike **JPA** the **@Id** annotation does not support the automatic generation of identifiers in the Java world. In this sample, the automatic increment behavior is handled by the Postgres database. 

File **Airplane.java**

    package com.mgu.r2dbc.entity;

    import lombok.Data;
    import org.springframework.data.annotation.CreatedDate;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.annotation.LastModifiedDate;
    import org.springframework.data.annotation.Version;
    
    import java.time.LocalDateTime;
    
    @Data
    public class AirPlane {
        @Id
        private Long id;
        private String name;
        private boolean workInProgress = false;
    
        @Version
        private Long version;
        @CreatedDate
        private LocalDateTime createdDate;
        @LastModifiedDate
        private LocalDateTime lastModifiedDate;
    
        public AirPlane() {}
     
        public AirPlane(Long id, String name, boolean workInProgress) {
            this.id = id;
            this.name = name;
            this.workInProgress = workInProgress;
        }
    }

File **FlightRoute.java**

    package com.mgu.r2dbc.entity;
    
    import lombok.Data;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.annotation.Transient;
    
    @Data
    public class FlightRoute {
        @Id
        private Long id;
        private Route route;
        private Long flightId;
     
        public FlightRoute() {
        }
     
        public FlightRoute(long fromStation, long toStation, long flightId) {
            Route route = new Route();
            route.fromStation = fromStation;
            route.toStation = toStation;
            this.route = route;
            this.flightId = flightId;
        }
    }

File **Station.java**

    package com.mgu.r2dbc.entity;

    import lombok.Data;
    import org.springframework.data.annotation.Id;
    
    @Data
    public class Station {
        @Id
        private Long id;
        private String iataCode;
        private String fullName;
    
        public Long getId() {
            return id;
        }
     
        public Station() {
        }
     
        public Station(String iataCode, String fullName) {
            this.iataCode = iataCode;
            this.fullName = fullName;
        }
    
        public String toString() {
            return "Station: " + id + "/" + iataCode + "/"+fullName;
        }
    }

## The repositories

Regarding the creation of the repositories, there is no real specificity. In a JPA world, we would extend **CrudRepository<T,ID>**,
with **R2DBC** we extend **ReactiveCrudRepository<T,ID>**.

We can define custom methods based on entity attributes (https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#r2dbc.repositories.queries).

We can define custom methods that rely on the **@Query** annotation. We just have to keep in mind that it is not possible at this time to use the entity 
inside the query (no JPQL support), we need to use the database names (table and column).

File **AirPlaneRepository.java**

    package com.mgu.r2dbc.repository;
    
    import org.springframework.data.repository.reactive.ReactiveCrudRepository;
    import com.mgu.r2dbc.AirPlane;
    import reactor.core.publisher.Mono;
    
    public interface AirPlaneRepository extends ReactiveCrudRepository<AirPlane, Long> {
        public Mono<AirPlane> findByName(String name);
    }

File **AirPlaneRepository.java**

    package com.mgu.r2dbc.repository;
    
    import org.springframework.data.r2dbc.repository.Query;
    import org.springframework.data.repository.reactive.ReactiveCrudRepository;
    
    import com.mgu.r2dbc.FlightRoute;
    
    import reactor.core.publisher.Flux;
    
    public interface FlightRouteRepository extends ReactiveCrudRepository<FlightRoute, Long> {
        @Query("SELECT f.* FROM flight_route f, air_plane ap where ap.name = :flightName AND ap.id = f.flight_id")
        public Flux<FlightRoute> findRoutesForFlight(String flightName);
    }

File **AirPlaneRepository.java**

    package com.mgu.r2dbc.repository;
    
    import java.util.Collection;
    
    import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
    import org.springframework.data.repository.reactive.ReactiveCrudRepository;
    
    import com.mgu.r2dbc.Station;
    
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;
    
    public interface StationRepository extends ReactiveCrudRepository<Station, Long>, ReactiveQueryByExampleExecutor<Station> {
        public Mono<Station> findByIataCode(String iataCode);
        public Flux<Station> findByIataCodeIn(Collection<String>iataCodes);
        public Flux<Station> findByFullNameLikeIgnoreCase(String fullName);
    }

## The service

Here the idea is to demonstrate the support of transactions in a reactive world.

The goal is to create a route between two airports for a plane. We will do this in 3 steps.

- we identify the airplane and lock it by setting the **work_in_progress** attribute to true
- we create the route
- we release the airplane by setting back the **work_in_progress** attribute to false

As we have a unicity constraint on the **flight_route** table, we are not allowed to put two airplanes on the same route. We 
can then easily create exceptions in the middle of the process and see what happens to the **work_in_progress** attribute.

File **ProgramService.java**

    package com.mgu.r2dbc.service;
    
    import java.util.Arrays;
    import java.util.List;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    
    import com.mgu.r2dbc.AirPlane;
    import com.mgu.r2dbc.FlightRoute;
    import com.mgu.r2dbc.Station;
    import com.mgu.r2dbc.AirPlaneRepository;
    import com.mgu.r2dbc.FlightRouteRepository;
    import com.mgu.r2dbc.StationRepository;
    import com.mgu.r2dbc.CreateRouteInput;
    
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
    
        public Mono<Boolean> createRoute(CreateRouteInput input) {
            Flux<Station> stations = stationRepository.findByIataCodeIn(Arrays.asList(input.stationFrom(), input.stationTo()));
            Mono<AirPlane> airPlane = airPlaneRepository.findByName(input.flightName())
                    .doOnNext(ap -> ap.setWorkInProgress(true))
                    .flatMap(airPlaneRepository::save);
     
            return Mono.zip(airPlane, stations.collectList(), (airplane, st) ->
                            Tuples.of(airPlane,
                                    new FlightRoute(getStationByCode(input.stationFrom(), st).getId(),
                                            getStationByCode(input.stationTo(), st).getId(),
                                            airplane.getId()))
                    )
                    .flatMap(t -> flightRouteRepository
                            .save(t.getT2())
                            .thenReturn(t.getT1()))
                    .flatMap(ap -> ap)
                    .doOnNext(ap -> ap.setWorkInProgress(false))
                    .flatMap(airPlaneRepository::save)
                    .thenReturn(Boolean.TRUE);
        }
     
        protected Station getStationByCode(String iataCode, List<Station> stations) {
            return stations
                    .stream()
                    .filter( station -> iataCode.equals(station.getIataCode()))
                    .findFirst()
                    .orElse(null);
        }
    }

For this **ProgramService** class, we need to define **CreateRouteInput** class that we will also use later to map the 
user input to a Java object.

File **CreateRouteInput.java**

    package com.mgu.r2dbc.web.request;
    
    public record CreateRouteInput(String flightName, String stationFrom, String stationTo) {
    }

Now everything regarding **R2DBC** is in place, let's expose it to easily test it.

# Web exposure

There is no big surprise in here. Please have a look at the relevant classes
**AirPlaneHandler**, **ProgramHandler**, **StationHandler** and **ApplicationRouter**.

# Test the application

Now we can run the application and play with all the endpoints.

All the endpoints are addressed using the HTTPie command-line tool, you can use whatever 
tool suits you, POSTMAN... Please keep in mind that if you prefer to develop a frontend 
application and use a browser to access the endpoints 
you will need to deal with CORS if you don't use any proxy and enable it.

## Simple requests

- http :8080/stations<br/>
This will return all stations, when starting the application you will have three results in a JSON format.
- http :8080/stations "Content-type:text/event-stream"<br/>
This will stream you all the stations.
- http :8080/airplanes or http :8080/airplanes "Content-type:text/event-stream"<br/>
This will return all the airplanes, when starting the application you will have three results.
- http :8080/routes or http :8080/routes "Content-type:text/event-stream"<br/>
This will return all the airplanes, when starting the application there will be NO result.
- echo -n '{"iataCode": "TLS", "fullName": "Toulouse"}' | http POST :8080/stations<br/>
This will create the TLS station. You can then call the /stations endpoint to check this new one is present.
- echo -n '{"flightName": "AF003", "stationFrom": "NCE", "stationTo": "CDG"}' | http POST :8080/routes<br/>
This will create a route between NCE and CDG and affect the flight AF003 to it.
You can then check the route is present with the /routes endpoints

## Next steps

[R2DBC and the transactions](02-r2dbc-transactions.md)

[R2DBC and Unit testing](03-r2dbc-junit.md)

[R2DBC and relations](04-r2dbc-relations.md)

[R2DBC and complex mappings](05-r2dbc-complex-mappings.md)

[R2DBC and queries](06-r2dbc-queries.md)
