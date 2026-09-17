package com.example.chatpdf.auth;

import java.util.UUID;

public record AuthResponse(String accessToken, String refreshToken, UUID familyId ) {
}
