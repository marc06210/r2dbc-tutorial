package com.mgu.r2dbc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.Optional;

@Data
@NoArgsConstructor
public class FlightRoute {

    @Data
    public class Route {
        private Long fromStation;
        private Long toStation;
    }

    @Id
    private Long id;
    private Route route;
    private Long flightId;

    @Transient
    private Station stationFrom;
    @Transient
    private Station stationTo;
    @Transient
    private AirPlane flight;

    public FlightRoute(long fromStation, long toStation, long flightId) {
        this.route = new Route();
        this.route.setFromStation(fromStation);
        route.setToStation(toStation);
        this.flightId = flightId;
    }

    public Long getFromStation() {
        return Optional.ofNullable(this.route)
                .map(Route::getFromStation)
                .orElse(null);
    }
 
    public void setFromStation(Long fromStation) {
        if(this.route==null) {
            this.route = new Route();
        }
        this.route.fromStation = fromStation;
    }
 
    public Long getToStation() {
        return Optional.ofNullable(this.route)
                .map(Route::getToStation)
                .orElse(null);
    }
 
    public void setToStation(Long toStation) {
        if(this.route==null) {
            this.route = new Route();
        }
        this.route.toStation = toStation;
    }
}
