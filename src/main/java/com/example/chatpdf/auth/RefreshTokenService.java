package com.example.chatpdf.auth;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final JwtService jwtService;
    public RefreshTokenService(RefreshTokenRepository repo,JwtService jwtService){
        this.repo=repo;
        this.jwtService=jwtService;
    }

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken(user));
        refreshToken.setUser(user);
        refreshToken.setFamilyId(UUID.randomUUID());
        refreshToken.setExpiry(LocalDateTime.now().plusDays(30));
        refreshToken.setRevoked(false);
        return repo.save(refreshToken);
    }

    public RefreshToken findByToken(String token){
        return repo.findByToken(token).orElseThrow(()->new RuntimeException("refresh token not found"));
    }
}
