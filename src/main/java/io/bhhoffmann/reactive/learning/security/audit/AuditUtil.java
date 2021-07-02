package io.bhhoffmann.reactive.learning.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

public class AuditUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuditUtil.class);

    private AuditUtil() {
        throw new IllegalStateException("Static utility class");
    }

    public static Mono<User> getAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .filter(User.class::isInstance)
            .cast(User.class)
            .doOnError(err -> logger.error("Could not extract user principal from SecurityContext. Error message: {}",
                err.getMessage()));
    }

}
