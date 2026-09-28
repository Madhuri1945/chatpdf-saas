package com.example.chatpdf.document.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;
    public List<String> chunk(String text){
        List<String> chunks=new ArrayList<>();
        int start=0;
        while(start<text.length()){
            int end=Math.min(start+CHUNK_SIZE,text.length());
            chunks.add(text.substring(start,end));
            if(end==text.length())break;
            start=end-OVERLAP;
        }
        return chunks;
    }
}
