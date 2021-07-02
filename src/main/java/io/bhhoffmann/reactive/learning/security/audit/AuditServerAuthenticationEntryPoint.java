package io.bhhoffmann.reactive.learning.security.audit;

import static io.bhhoffmann.reactive.learning.logging.LogUtil.AUDIT_MARKER;
import static io.bhhoffmann.reactive.learning.logging.LogUtil.logOnNext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ServerAuthenticationEntryPoint: Used by Spring Security as a method for starting an authentication process if a
 * request without the required authentication data is received (e.g. redirect, WWW-Authorization header). Used by the
 * AuthenticationWebFilter's ServerAuthenticationFailureHandler.
 * <p>
 * We use it as a way to intercept and log requests without authentication for auditing purposes.
 * <p>
 * Must be set as the ServerAuthenticationEntryPoint to be used by the AuthenticationWebFilter in question (default
 * httpBasic filter managed through configuration, directly on custom filters)
 */
public class AuditServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker auditMarker = MarkerFactory.getMarker(AUDIT_MARKER);

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.justOrEmpty(exchange.getRequest())
            .filter(request -> !request.getHeaders().isEmpty())
            .doOnEach(logOnNext(request -> logger.warn(auditMarker,
                "AUTHENTICATION FAILED - details={path={}, headers={}}", request.getPath(), request.getHeaders())))
            .then(super.commence(exchange, ex));
    }

}
