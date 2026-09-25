package com.example.chatpdf.exceptions.auth;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(){
        super("Email already exists");
    }
}
