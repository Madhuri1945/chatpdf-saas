package com.example.chatpdf.exceptions.auth;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(){
        super("Invalid token");
    }
}
