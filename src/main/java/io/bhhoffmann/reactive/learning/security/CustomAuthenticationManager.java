package io.bhhoffmann.reactive.learning.security;

import static io.bhhoffmann.reactive.learning.security.SecurityConfiguration.SOME_OTHER_AUTHORITY;

import java.util.Collections;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

/**
 * Use the extracted data passed from the CustomServerAuthenticationConverter to authenticate and authorize the request
 * however you want. In this example nothing is done, a token is simply always created with the principal 'user',
 * credentials 'password' and authority SOME_OTHER_AUTHORITY
 */
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
            .map(Authentication::getCredentials)
            .map(String::valueOf)
            .map(s -> new UsernamePasswordAuthenticationToken("user", "password",
                Collections.singletonList(new SimpleGrantedAuthority(SOME_OTHER_AUTHORITY))));
    }

}
