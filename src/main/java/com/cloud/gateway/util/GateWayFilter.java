package com.cloud.gateway.util;

import com.cloud.gateway.config.GatewayConfig;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class GateWayFilter implements GatewayFilter {
    @Autowired
    private JWTService jwtService;
    Logger logger = LoggerFactory.getLogger(GateWayFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info(request.getId()+" Method"+request.getMethod()+" Header-"+request.getHeaders()+" URI-" +request.getURI());

        final List<String> apiEndpoints = List.of("/signin", "/signup", "/swagger-ui", "/swagger", "/api-docs", "/actuator");

        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            final String token = request.getHeaders().getOrEmpty("Authorization").get(0).substring(7);
            Claims claims;
            try {
                claims = jwtService.decodeJWT(token);
            } catch (Exception e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return response.setComplete();
            }
            exchange.
                    getRequest()
                    .mutate()
                    .header("id", String.valueOf(claims.getId()))
                    .header("role", String.valueOf(claims.getSubject()))
                    .build();
        }

        return chain.filter(exchange);
    }

}
