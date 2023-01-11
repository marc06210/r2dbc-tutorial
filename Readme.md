# What it is about

This is a sample application using R2DBC the reactive version of JDBC.

This application plugs to a posgresql database, so you need to have one instance running
to have a working application.

Then you can run the following requests (requests done using HTTPie):

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

# Additional resources

The other md files from this repository ca be used as a tutorial to put in place and understand
basic concepts of R2DBC. The entry point is the [first document](01-r2dbc-introduction.md).


# Notes

## R2DBC and JDBC in the same application
At this moment it is not possible to mix R2DBC and JDBC in the same application. It is not possible because of the lava antipattern.

In our case, our lava mix is because we could do the same things by activating quite the same feature from two different dependencies.

So to have both dependencies in the same application could be possible but using a very complex code (not easy to read) and the risk of facing a misconception architecture is very high.

## R2DBC and liquibase
Liquibase, like many other database version control tools, relies on JDBC.

There is right now no support for liquibase with R2DBC.

## Log queries
Just like for JPA you can dump your SQL requests by setting log levels. There are two parameters:

- io.r2dbc.<your_db_provider>.QUERY<br/>
your_db_provider must be replaced by the correct driver (ie. h2, postgresql,...)
when set to DEBUG then the SQL requests are logged without dumping the injected parameters<br/>
=> 2022-03-08 08:45:26.411 DEBUG 96000 --- [ctor-http-nio-5] io.r2dbc.postgresql.QUERY                : Executing query: SELECT station.* FROM station WHERE (UPPER(station.full_name) LIKE UPPER($1))

- io.r2dbc.<your_db_provider>.PARAM<br/>
your_db_provider must be replaced by the correct driver (ie. h2, postgresql,...)
when set to DEBUG then the injected parameters are also logged<br/>
=> 2022-03-08 08:45:26.411 DEBUG 96000 --- [ctor-http-nio-5] io.r2dbc.postgresql.PARAM                : Bind parameter [0] to: %paris%
