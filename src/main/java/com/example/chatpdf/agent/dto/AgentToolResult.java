package com.example.chatpdf.agent.dto;

public record AgentToolResult(
        boolean success,
        String errorCode,
        String message,
        Object data
) {
    public static AgentToolResult success(Object data){
        return new AgentToolResult(true,null,"Success",data);
    }
    public static AgentToolResult failure(String errorCode,String message){
        return new AgentToolResult(false,errorCode,message,null);
    }
}
