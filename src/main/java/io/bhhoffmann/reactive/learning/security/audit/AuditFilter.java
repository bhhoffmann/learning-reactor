package io.bhhoffmann.reactive.learning.security.audit;

import static io.bhhoffmann.reactive.learning.logging.LogUtil.AUDIT_MARKER;
import static io.bhhoffmann.reactive.learning.logging.LogUtil.logOnNext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A standard Webflux webfilter used to audit all authorized requests (username and authorities). Placed as the final
 * filter to be executed (right before request is handed to controller) so that we are sure that the request has passed
 * through the Spring Security Webfilter chain.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditFilter implements WebFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker auditMarker = MarkerFactory.getMarker(AUDIT_MARKER);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain webFilterChain) {
        return AuditUtil.getAuthenticatedUser()
            .doOnEach(logOnNext(
                user -> logger.info(auditMarker,
                    "AUTHORIZED - details={path={}, principal={username={}, authorities={}}}}",
                    exchange.getRequest().getPath(), user.getUsername(), user.getAuthorities())))
            .then(webFilterChain.filter(exchange));
    }
}
