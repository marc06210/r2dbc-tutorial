package com.mgu.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Data
public class FlightRoute {
    public class Route {
        Long fromStation;
        Long toStation;
    }

    @Id
    private Long id;
    private Route route;
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
        Route route = new Route();
        route.fromStation = fromStation;
        route.toStation = toStation;
        this.route = route;
        this.flightId = flightId;
    }

    public Long getFromStation() {
        return this.route != null ? this.route.fromStation : null;
    }
 
    public void setFromStation(Long fromStation) {
        if(this.route==null) {
            this.route = new Route();
        }
        this.route.fromStation = fromStation;
    }
 
    public Long getToStation() {
        return this.route != null ? this.route.toStation : null;
    }
 
    public void setToStation(Long toStation) {
        if(this.route==null) {
            this.route = new Route();
        }
        this.route.toStation = toStation;
    }

}