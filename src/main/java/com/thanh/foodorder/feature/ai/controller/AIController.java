package com.thanh.foodorder.feature.ai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.thanh.foodorder.feature.ai.dto.AiChatRequest;
import com.thanh.foodorder.feature.ai.dto.AiChatResponse;
import com.thanh.foodorder.feature.ai.service.AiService;


@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AiService aiService;

    public AIController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        AiChatResponse answer = aiService.getChatResponse(request.getMessage());

        return ResponseEntity.status(HttpStatus.OK).body(answer);
    }
}