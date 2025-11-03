package com.tourplanner.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/*
    Get the token -> extract the email (username) from the JWT
    Then fetch the user from the DB using customUserDetailsService
    Wrap the UserDetails in a UsernamePasswordAuthenticationToken so Spring Security knows
    this request is authenticated and can access roles/authorities
    Continue the filter chain with this authentication in the reactive security context
*/


@Component
public class JwtAuthenticationFilter implements WebFilter {
    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Skip token validation for public routes to prevent 403 forbidden error
        if (path.equals("/graphql") || path.startsWith("/graphiql")) {
            String token = getJWTFromRequest(exchange.getRequest());
            if(token == null) {
                return chain.filter(exchange);
            }
        }

        String token = getJWTFromRequest(exchange.getRequest());
        if(token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUserNameFromJWT(token);

            return customUserDetailsService.findByUsername(username)
                    .flatMap(userDetails -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    });
        }
        return chain.filter(exchange);
    }

    private String getJWTFromRequest(ServerHttpRequest request) {
         /*
            Authorization : Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhc2hpc2hAZ21haWwuY29tIiwiaWF0IjoxNzUzOTYyOTEzLCJleHAiOjE3NTM5NjI5ODMsInVzZXJ0eXBlIjoiVVNFUiJ9.B8hssQI6t3IzekmIHj9TzCGCYHb1d_vJKMyA6Rjb_xE
        */
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
