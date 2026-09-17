package com.example.chatpdf.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {
    private final UserRepository repository;
    public CustomerDetailsService(UserRepository repository){
        this.repository=repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)throws UsernameNotFoundException{
        User user=repository.findByEmail(email).orElseThrow(()->
                new UsernameNotFoundException("user not found"));
        return new CustomUserDetails(user);
    }
}
