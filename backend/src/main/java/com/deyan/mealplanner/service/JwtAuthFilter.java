package com.deyan.mealplanner.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request once, extracts the JWT from the
 * “Authorization: Bearer …” header, validates it, and – if valid –
 * populates the SecurityContext with the {@link UserDetails} returned by
 * {@link MyUserDetailsService}.
 *
 * <p>The token may contain:
 * <ul>
 *   <li><b>sub</b> (email) – existing behaviour</li>
 *   <li><b>userId</b> (numeric) – optional extra claim you add when
 *       signing the token</li>
 * </ul>
 * Only <em>sub</em> is needed to authenticate; userId is handy later if
 * you want to expose it via <code>@AuthenticationPrincipal</code>.</p>
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, MyUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String token = authHeader.substring(7);      // strip "Bearer "

        final String email = jwtUtil.extractEmail(token);  // sub = email

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // validate signature, expiry, and subject matches
            if (jwtUtil.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // principal
                                null,                           // credentials
                                userDetails.getAuthorities()    // roles
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
