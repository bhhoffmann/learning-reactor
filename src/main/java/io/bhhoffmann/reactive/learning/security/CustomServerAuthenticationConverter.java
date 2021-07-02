package io.bhhoffmann.reactive.learning.security;

import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomServerAuthenticationConverter implements ServerAuthenticationConverter {

    /**
     * Extract whatever data from the request you want to use for authentication and authorization. In this example it
     * is the header 'custom-header'
     */
    @Override
    public Mono<Authentication> convert(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange)
            .map(exchange -> exchange.getRequest().getHeaders())
            .map(httpHeaders -> httpHeaders.getOrEmpty("custom-header").stream().findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(encodedSaml -> new UsernamePasswordAuthenticationToken(encodedSaml, encodedSaml));
    }

}
