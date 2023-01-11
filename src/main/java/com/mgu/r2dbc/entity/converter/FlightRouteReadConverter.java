package com.mgu.r2dbc.entity.converter;

import com.mgu.r2dbc.entity.FlightRoute;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class FlightRouteReadConverter implements Converter<Row, FlightRoute> {
    @Override
    public FlightRoute convert(Row source) {
        FlightRoute route = new FlightRoute();
        route.setId(source.get("id", Long.class));
        route.setFlightId(source.get("flight_id", Long.class));
        route.setToStation(source.get("to_station", Long.class));
        route.setFromStation(source.get("from_station", Long.class));
        return route;
    }
}
