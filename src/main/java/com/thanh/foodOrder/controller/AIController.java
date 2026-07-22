package com.thanh.foodorder.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.dto.request.AiChatRequest;
import com.thanh.foodorder.dto.response.AiChatResponse;
import com.thanh.foodorder.service.ProductAiTool;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final ChatClient chatClient;
    private final ProductAiTool productAiTool;

    public AIController(ChatClient chatClient, ProductAiTool productAiTool) {
        this.chatClient = chatClient;
        this.productAiTool = productAiTool;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody AiChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.getMessage())
                .tools(productAiTool)
                .call()
                .content();

        return new AiChatResponse(answer);
    }
}