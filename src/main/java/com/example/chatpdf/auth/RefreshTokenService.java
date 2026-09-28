package com.example.chatpdf.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
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

    public void validateRefreshToken(RefreshToken token){
        if(token.isRevoked()){
            revokeFamily(token.getFamilyId());
            throw new RuntimeException("Refresh token reuse detected");
        }
        if(token.getExpiry().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
        }
    }
    public void revokeToken(RefreshToken token){
        token.setRevoked(true);
        repo.save(token);
    }
    public RefreshToken rotateToken(RefreshToken oldToken){
        RefreshToken newToken=new RefreshToken();
        newToken.setToken(jwtService.generateRefreshToken(oldToken.getUser()));
        newToken.setUser(oldToken.getUser());
        newToken.setFamilyId(oldToken.getFamilyId());
        newToken.setExpiry(LocalDateTime.now().plusDays(30));
        newToken.setRevoked(false);
        return repo.save(newToken);
    }

    @Transactional
    public void revokeFamily(UUID familyId){
        List<RefreshToken> tokens=repo.findByFamilyId(familyId);
        for(RefreshToken token:tokens){
         token.setRevoked(true);
        }
        repo.saveAll(tokens);
    }

    @Transactional
    public void logout(String refreshToken){
        RefreshToken token=findByToken(refreshToken);
        revokeToken(token);
    }

//    public void logoutAll(String refreshToken){
//        RefreshToken token=findByToken(refreshToken);
//        revokeFamily(token.);
//    }
}
