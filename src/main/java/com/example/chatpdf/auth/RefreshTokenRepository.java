package com.example.chatpdf.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByFamilyId(UUID familyId);
    List<RefreshToken> findByUserAndRevokedFalse(User user);
}
