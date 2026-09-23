package com.example.chatpdf.document.exception;

public class FileTooLargeException extends RuntimeException{
    public FileTooLargeException() {
        super("Maximum file size is 20 MB");
    }
}
