package com.example.chatpdf.exceptions.common;

public class DocumentNotReadyException extends RuntimeException {
    public DocumentNotReadyException(String message){
        super(message);
    }
}
