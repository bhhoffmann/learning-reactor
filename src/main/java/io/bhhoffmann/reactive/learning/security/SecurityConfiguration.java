package io.bhhoffmann.reactive.learning.security;

import io.bhhoffmann.reactive.learning.security.audit.AuditServerAccessDeniedHandler;
import io.bhhoffmann.reactive.learning.security.audit.AuditServerAuthenticationEntryPoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@EnableWebFluxSecurity
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    public static final String ADMIN_AUTHORITY = "ADMIN";
    public static final String SOME_OTHER_AUTHORITY = "SOME_AUTHORITY";

    private static final String ADMIN_USERNAME = "admin";

    private static UserDetails createUserDetails(
        String userName,
        String password,
        String... authorities
    ) {
        return User
            .withUsername(userName)
            .password(password)
            .authorities(authorities)
            .build();
    }

    private static Optional<String> validPassword(String password) {
        return Optional.ofNullable(password)
            .filter(pw -> !pw.isBlank());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("sha256", new StandardPasswordEncoder());

        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(
        @Value("${basic-auth.admin-password}") String adminPassword
    ) {
        return new MapReactiveUserDetailsService(Stream
            .of(
                validPassword(adminPassword)
                    .map(pw -> createUserDetails(ADMIN_USERNAME, pw, ADMIN_AUTHORITY, SOME_OTHER_AUTHORITY))
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toUnmodifiableList())
        );
    }

    @Bean
    public SecurityWebFilterChain defaultSecurityFilterChain(
        ServerHttpSecurity httpSecurity
    ) {
        /*
        return httpSecurity
            //Disable Spring Security functionality that is not needed if you only have REST APIs
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .headers().frameOptions().mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
            .and() //Continue configuring ServerHttpSecurity

            //Enable basic auth (use default Spring Security filter). I.e. extract username and password from the Authorization header and perform Authentication using these
            .httpBasic()
            .authenticationEntryPoint(
                new AuditServerAuthenticationEntryPoint()) //What to do with unauthenticated basic auth requests
            .and()

            //Add custom security filter to do authentication and authorization (e.g. handle JWT)
            .addFilterAfter(customAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)

            //Set exception handling
            .exceptionHandling()
            .accessDeniedHandler(new AuditServerAccessDeniedHandler()) //What to do with unauthorized requests
            .authenticationEntryPoint(new AuditServerAuthenticationEntryPoint()) //What to do with unauthenticated requests
            .and()

            //Define the access rights (authorities) required for each endpoint
            //The default Spring AntPathMatcher used by .pathMatchers() matches URLs using the following rules:
            //? matches one character
            //* matches zero or more characters
            //** matches zero or more directories in a path
            //{spring:[a-z]+} matches the regexp [a-z]+ as a path variable named "spring"
            .authorizeExchange() //For exchanges (requests)
            .pathMatchers("/open-for-everyone") //that matches this url
            .permitAll() //permit all requests
            .pathMatchers("/some-path") //that matches this url
            .hasAnyAuthority(ADMIN_AUTHORITY, SOME_OTHER_AUTHORITY) // permit only those users that have at least one of these authorities
            .anyExchange() //For any exchange (all requests) that does not match any of the above
            .hasAnyAuthority(ADMIN_AUTHORITY)
            .and()
            .build(); //Complete configuration
        */


        //Disable security
        return httpSecurity
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .headers().frameOptions().mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
            .and()
            .authorizeExchange()
            .anyExchange()
            .permitAll()
            .and()
            .build();
    }

    private AuthenticationWebFilter customAuthenticationFilter() {
        AuthenticationWebFilter webFilter = new AuthenticationWebFilter(new CustomAuthenticationManager());
        webFilter.setServerAuthenticationConverter(new CustomServerAuthenticationConverter());
        webFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/some-path/**")); //The paths for which this filter should be applied
        webFilter.setAuthenticationFailureHandler(
            new ServerAuthenticationEntryPointFailureHandler(new AuditServerAuthenticationEntryPoint())
        );

        return webFilter;
    }

}
