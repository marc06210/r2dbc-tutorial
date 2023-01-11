It is possible to define complex mappings.

Let's consider the **FlightRoute** entity. It is a flat object where the id of the origin station and the id of the 
destination station are at the top level. Now let's imagine we want to create a logical entity Route to hold those data.

This will give us the following entity

    public class FlightRoute {
        public class Route {
            Long fromStation;
            Long toStation;
        }
    
        @Id
        private Long id;
        private Route route;
        private Long flightId;
        ...
    }

With that structure, there is no way **Spring** and **R2DBC** will be able to insert/retrieve data to/from the database.

We need to create converters to cover such a feature.

First, let's create a converter able to insert data. Our class must be annotated **@WritingConverter** and must implement 
the **Converter<S, T>** interface.

    package com.mgu.r2dbc.entity.converter
    
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

Then, let's create the converter that will be invoked when creating the entity from a database row. This class will of 
course implement the **Converter<S, T>** interface but this time it will be annotated **@ReadingConverter**.

    package com.mgu.r2dbc.entity.converter
    
    import com.afklm.tecc.r2dbc.entity.FlightRoute;
    import io.r2dbc.spi.Row;
    import org.springframework.core.convert.converter.Converter;
    import org.springframework.data.convert.ReadingConverter;
    import org.springframework.data.convert.WritingConverter;
    import org.springframework.data.r2dbc.mapping.OutboundRow;
    import org.springframework.r2dbc.core.Parameter;
    
    @ReadingConverter
    public class FlightRouteReadConverter implements Converter<Row, FlightRoute> {
        @Override
        public FlightRoute convert(Row source) {
            FlightRoute route = new FlightRoute();
            route.setId(source.get("id", Long.class));
            route.setFlightId(source.get("flight_id", Long.class));
            route.setToStation(Long.valueOf(source.get("to_station", String.class)));
            route.setFromStation(Long.valueOf(source.get("from_station", String.class)));
            return route;
        }
    }

The final step is to register those **Converters** into our **R2DBC** configuration. This is done very simply with the 
following bean. You can define it in your main application class.

    import org.springframework.core.convert.converter.Converter;
    import org.springframework.data.convert.CustomConversions;
    import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
    import org.springframework.data.r2dbc.dialect.PostgresDialect;
    
    ...

    @Bean
    public R2dbcCustomConversions customConversions() {
        List<Converter<?,?>> converters = Arrays.asList(new FlightRouteReadConverter(), new FlightRouteWriteConverter());
        // this one defines converter that are activated only for Postgresql
        //return R2dbcCustomConversions.of(PostgresDialect.INSTANCE,converters);
        // this one defines converters used all the time
        return new R2dbcCustomConversions(CustomConversions.StoreConversions.NONE,converters);
    }