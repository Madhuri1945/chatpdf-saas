package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.LLMFunctionDefinition;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.dto.SearchPdfArguments;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.service.DocumentService;
import com.example.chatpdf.document.service.SimilaritySearchService;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class SearchPdfTool implements AgentTool{
    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final SimilaritySearchService similaritySearchService;
    public SearchPdfTool(ObjectMapper objectMapper, DocumentService documentService, SimilaritySearchService similaritySearchService) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.similaritySearchService = similaritySearchService;
    }

    public String getName(){
        return "search_pdf";
    }

    @Override
    public String getDescription() {
        return "Search the user's PDF for information relevant to a query.";
    }

    @Override
    public LLMTool getDefinition(){
        Map<String,Object> queryProperty=Map.of(
                "type","string",
                "description","The question or keywords to search for in the pdf."
        );
        Map<String,Object> parameters=Map.of(
                "type","object",
                "properties",Map.of("query",queryProperty),
                "required",List.of("query"),
                "additionalProperties",false
        );
        LLMFunctionDefinition function=new LLMFunctionDefinition(
                "search_pdf",
                "Search the user's PDF for relevant information.",
                parameters,true

        );
        return new LLMTool("function",function);
    }
    @Override
    public Object execute(
            String arguments,Long document_id,Long userId
    ){
       try{
           SearchPdfArguments input=objectMapper.readValue(
                   arguments,SearchPdfArguments.class
           );
           if(input.query()==null || input.query().isBlank()){
                throw new IllegalArgumentException(
                        "Search query cannot be empty"
                );
           }
           Document doc=documentService.getOwnedDocument(document_id,userId);
           if(doc.getStatus()!= Status.READY){
               throw new DocumentNotReadyException("Document is unavailable to search");
           }
           List<DocumentChunk> chunks=similaritySearchService.search(document_id,input.query(),5);
           return chunks.stream().map(
                   chunk-> Map.of(
                           "chunkId",chunk.getId(),
                           "chunkIndex",chunk.getChunkIndex(),
                           "content",chunk.getContent()
                   )
           ).toList();

       }catch(com.fasterxml.jackson.core.JsonProcessingException  e){
           throw new IllegalArgumentException("Invalid search tool argument",e);
       }
    }


}
