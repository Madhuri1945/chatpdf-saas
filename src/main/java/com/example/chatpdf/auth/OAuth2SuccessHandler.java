package com.example.chatpdf.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper mapper;
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    public OAuth2SuccessHandler(UserRepository userRepo,JwtService jwtService,RefreshTokenService refreshTokenService,
                                ObjectMapper mapper){
        this.userRepo=userRepo;
        this.jwtService=jwtService;
        this.refreshTokenService=refreshTokenService;
        this.mapper=mapper;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException{
        OAuth2User googleUser=(OAuth2User) authentication.getPrincipal();
        String email=googleUser.getAttribute("email");
        String name=googleUser.getAttribute("name");
        if(email==null || email.isBlank()){
            throw new RuntimeException("Google account has no email");
        }
        Optional<User> existing = userRepo.findByEmail(email);
        User user=existing.orElseGet(()->{
            User newUser=new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setRole(Role.USER);
            newUser.setProvider(Provider.GOOGLE);
            return userRepo.save(newUser);
        });
        String access= jwtService.generateAccessToken(user);
        RefreshToken refresh=refreshTokenService.createRefreshToken(user);
        AuthResponse dto=new AuthResponse(access,refresh.getToken(),refresh.getFamilyId());
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(),dto);
    }
}
