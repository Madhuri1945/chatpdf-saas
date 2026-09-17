package com.example.chatpdf.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomerDetailsService userDetailsService;
    public JwtAuthenticationFilter(
            JwtService jwtService,CustomerDetailsService userDetailsService
    ){
        this.jwtService=jwtService;
        this.userDetailsService=userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader("Authorization");
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        String token=authHeader.substring(7);
        String email=jwtService.extractUsername(token);
        if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails user=userDetailsService.loadUserByUsername(email);
            if(jwtService.isTokenValid(token,user)){
                UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(
                        user,null,user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request,response);
    }
}
