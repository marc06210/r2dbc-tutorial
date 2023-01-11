package com.mgu.r2dbc.entity.converter;

import com.mgu.r2dbc.entity.FlightRoute;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

@WritingConverter
public class FlightRouteWriteConverter implements Converter<FlightRoute, OutboundRow> {
    @Override
    public OutboundRow convert(FlightRoute flightRoute) {
        OutboundRow row = new OutboundRow();
        if(flightRoute.getId() != null) {
            row.put("id", Parameter.from(flightRoute.getId()));
        }
        row.put("flight_id", Parameter.from(flightRoute.getFlightId()));
        row.put("from_station", Parameter.from(flightRoute.getFromStation()));
        row.put("to_station", Parameter.from(flightRoute.getToStation()));
        return row;
    }
}
