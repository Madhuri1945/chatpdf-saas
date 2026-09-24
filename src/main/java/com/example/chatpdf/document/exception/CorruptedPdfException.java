package com.example.chatpdf.document.exception;

public class CorruptedPdfException extends RuntimeException{
    public CorruptedPdfException(){
        super("Corrupted pdf");
    }
}
