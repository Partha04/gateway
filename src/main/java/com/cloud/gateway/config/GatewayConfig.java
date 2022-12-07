package com.cloud.gateway.config;

import com.cloud.gateway.util.GateWayFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Value("${userservice.url}")
    private String userserviceurl;
    @Autowired
    private GateWayFilter gateWayFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user", r -> r.path("/user/**").filters(f -> f.filter(gateWayFilter)).uri(userserviceurl))
                .build();
    }

}