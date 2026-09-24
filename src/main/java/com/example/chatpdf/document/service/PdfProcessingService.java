package com.example.chatpdf.document.service;

import com.example.chatpdf.document.exception.CorruptedPdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfProcessingService {
    public String extractText(String storagePath){
        try(PDDocument pdf= Loader.loadPDF(new File(storagePath))){
            PDFTextStripper stripper=new PDFTextStripper();
            return stripper.getText(pdf);
        }catch(IOException e){
            throw new CorruptedPdfException();
        }
    }
}
