package com.mgu.r2dbc.web.request;

import com.mgu.r2dbc.entity.FlightRoute;

/**
 * Body input needed to create a {@link FlightRoute}.
 */
public record CreateRouteInput(String flightName, String stationFrom, String stationTo) {
}

