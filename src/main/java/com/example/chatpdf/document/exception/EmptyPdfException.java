package com.example.chatpdf.document.exception;

public class EmptyPdfException extends RuntimeException{
    public EmptyPdfException(){
        super("No readable text found");
    }
}
