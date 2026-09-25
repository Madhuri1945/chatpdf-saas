package com.example.chatpdf.exceptions.auth;

public class RefreshTokenExpiredException extends RuntimeException{

    public RefreshTokenExpiredException(){
        super("Refresh token expired");
    }
}
