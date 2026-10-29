package com.example.chatpdf.agent.tool;

import com.example.chatpdf.exceptions.tools.ToolNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ToolDispatcher {
    private final ToolRegistry tools;

    public ToolDispatcher(ToolRegistry tools) {
        this.tools = tools;
    }

    public Object dispatch(String toolName,String arguments,Long documentId,Long userId){
        AgentTool tool=tools.getTool(toolName);

        return tool.execute(arguments,documentId,userId);
    }
}
