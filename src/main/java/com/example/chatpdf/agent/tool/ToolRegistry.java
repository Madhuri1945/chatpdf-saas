package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.exceptions.tools.ToolNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolRegistry {

    private final Map<String,AgentTool> tools=new HashMap<>();

    public ToolRegistry(List<AgentTool> availableTools) {
        for(AgentTool tool:availableTools){
            String toolName=tool.getName();
            if(tools.containsKey(toolName)){
                throw new IllegalStateException(
                        "Duplicate tool name"+toolName
                );
            }
            tools.put(toolName,tool);
        }
    }



    public AgentTool getTool(String toolName){
        AgentTool tool=tools.get(toolName);
        if(tool==null){
            throw new ToolNotFoundException("Unknown tool:"+toolName);
        }
        return tool;
    }

    public List<LLMTool> getDefinitions(){
       return tools.values().stream().map(AgentTool::getDefinition)
               .toList();
    }
}
