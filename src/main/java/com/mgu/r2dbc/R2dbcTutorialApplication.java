package com.mgu.r2dbc;

import com.mgu.r2dbc.entity.converter.FlightRouteReadConverter;
import com.mgu.r2dbc.entity.converter.FlightRouteWriteConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(exclude = WebSessionIdResolverAutoConfiguration.class)
@EnableR2dbcAuditing
public class R2dbcTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(R2dbcTutorialApplication.class, args);
    }

    //@Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("createDatabase.sql")));
        return initializer;
    }

    @Bean
    public R2dbcCustomConversions customConversions() {
        List<Converter<?,?>> converters = Arrays.asList(new FlightRouteReadConverter(), new FlightRouteWriteConverter());
        // this one defines converter that are activated only for Postgresql
        //return R2dbcCustomConversions.of(PostgresDialect.INSTANCE,converters);
        // this one defines converters used all the time
        return new R2dbcCustomConversions(CustomConversions.StoreConversions.NONE,converters);
    }
}
