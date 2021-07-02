package io.bhhoffmann.reactive.learning.security.audit;

import static io.bhhoffmann.reactive.learning.logging.LogUtil.AUDIT_MARKER;
import static io.bhhoffmann.reactive.learning.logging.LogUtil.logOnNext;

import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ServerAccessDeniedHandler: Used by Spring Security as a method for handling AccessDeniedExceptions that are thrown
 * when an authenticated user tries to use a resource it does not have access to (not correct authorities)
 * <p>
 * We use it as a way to intercept and log requests that are unauthorized.
 * <p>
 * Must be set as the ServerAccessDeniedHandler to be used by the AuthenticationWebFilter in question (default httpBasic
 * filter managed through configuration, directly on custom filters)
 */
public class AuditServerAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Marker auditMarker = MarkerFactory.getMarker(AUDIT_MARKER);

    //Inspired by HttpStatusServerAccessDeniedHandler https://github.com/spring-projects/spring-security/blob/master/web/src/main/java/org/springframework/security/web/server/authorization/HttpStatusServerAccessDeniedHandler.java
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        return AuditUtil.getAuthenticatedUser()
            .doOnEach(logOnNext(
                user -> logger.warn(auditMarker,
                    "ACCESS DENIED - details={path={}, principal={username={}, authorities={}}}",
                    exchange.getRequest().getPath(), user.getUsername(), user.getAuthorities())))
            .then(Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap(response -> {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
                DataBufferFactory dataBufferFactory = response.bufferFactory();
                DataBuffer buffer = dataBufferFactory.wrap(ex.getMessage().getBytes(Charset.defaultCharset()));
                return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
            }));
    }
}
