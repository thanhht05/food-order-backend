package com.thanh.foodorder.feature.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import com.thanh.foodorder.feature.ai.dto.AiChatResponse;
import com.thanh.foodorder.feature.ai.dto.GroqMessage;
import com.thanh.foodorder.feature.ai.dto.GroqRequest;
import com.thanh.foodorder.feature.ai.dto.GroqResponse;
import com.thanh.foodorder.feature.ai.dto.ProductAiResponse;
import com.thanh.foodorder.feature.ai.dto.ProductSearchCriteria;
import com.thanh.foodorder.feature.product.service.ProductService;
import com.thanh.foodorder.feature.user.domain.User;

@Service
public class AiService {
        private final ObjectMapper objectMapper;
        private final ProductService productService;

        public AiService(ObjectMapper objectMapper, ProductService productService,
                        RestClient.Builder restClientBuilder) {
                this.objectMapper = objectMapper;
                this.productService = productService;
                this.restClient = restClientBuilder.build();

        }

        @Value("${groq.api.url}")
        private String apiUrl;

        @Value("${groq.api.key}")
        private String apiKey;

        @Value("${groq.api.model}")
        private String model;

        private final RestClient restClient;

        public AiChatResponse getChatResponse(String userMessage) {

                try {
                        ProductSearchCriteria criteria = extractCriteria(userMessage); // convert user message to DTO

                        List<ProductAiResponse> products = productService.search(criteria); // get product from db

                        if (products.isEmpty()) {
                                return new AiChatResponse(
                                                "Xin lỗi, tôi chưa tìm thấy món ăn phù hợp "
                                                                + "với yêu cầu của bạn.",
                                                List.of());
                        }

                        String aiMessage = generateRecommendation(userMessage, products); // get result from AI

                        return new AiChatResponse(
                                        aiMessage,
                                        products);

                } catch (Exception e) {
                        e.printStackTrace();

                        return new AiChatResponse(
                                        "Xin lỗi, hệ thống tư vấn hiện đang bận. "
                                                        + "Vui lòng thử lại sau!",
                                        List.of());
                }
        }

        /*
         * đã có product:
         * {
         * Long id,
         * String name,
         * Double price,
         * String category,
         * String description
         * }
         * và user prompt: User Prompt -> "Tôi muốn ăn gà cay dưới 100k"
         */

        private String generateRecommendation(
                        String userMessage,
                        List<ProductAiResponse> products) throws JsonProcessingException {

                String productJson = objectMapper.writeValueAsString(products);// convert list product to JSON

                String systemPrompt = """
                                Bạn là nhân viên tư vấn của ứng dụng đặt đồ ăn.

                                Quy tắc bắt buộc:
                                - Xưng hô với khách hàng là "bạn" và xưng là "Shop của chúng tôi" (ví dụ: Shop của chúng tôi giúp có sản phẩm xxx phù hợp với yêu cầu bảu bạn )
                                - Chỉ được tư vấn sản phẩm có trong danh sách được cung cấp.
                                - Không tự tạo thêm món ăn.
                                - Chỉ đưa ra 4 sản phẩm.
                                - Phải ghi rõ tên sản phẩm không cần ghi giá sản phẩm.
                                - Giải thích ngắn gọn vì sao sản phẩm phù hợp.
                                - Trả lời bằng tiếng Việt.
                                - Giọng văn thân thiện, ngắn gọn.
                                """;

                String userPrompt = """
                                Yêu cầu của khách hàng:
                                %s

                                Danh sách sản phẩm lấy từ database:
                                %s
                                """.formatted(userMessage, productJson);

                GroqRequest request = new GroqRequest(
                                model,
                                List.of(
                                                new GroqMessage("system", systemPrompt),
                                                new GroqMessage("user", userPrompt)));

                GroqResponse response = restClient.post()
                                .uri(apiUrl)
                                .header("Authorization", "Bearer " + apiKey)
                                .header("Content-Type", "application/json")
                                .body(request)
                                .retrieve()
                                .body(GroqResponse.class);

                if (response == null
                                || response.choices() == null
                                || response.choices().isEmpty()
                                || response.choices().get(0).message() == null) {
                        throw new RuntimeException(
                                        "Groq không trả về nội dung tư vấn");
                }

                String content = response.choices()// ai will response many choices. SO get 0 to get firt response
                                .get(0)
                                .message()
                                .content();

                if (content == null || content.isBlank()) {
                        throw new RuntimeException(
                                        "Nội dung tư vấn bị rỗng");
                }

                return content;
        }

        private ProductSearchCriteria extractCriteria(String userMessage) { // Nhờ AI chuyển từ user prompt DTO
                                                                            // SeatchCriteria

                String systemPrompt = """
                                Bạn có nhiệm vụ phân tích yêu cầu tìm món ăn.

                                Chỉ trả về JSON hợp lệ, không giải thích, không markdown.

                                Cấu trúc:
                                {
                                  "keyword": string hoặc null,
                                  "category": string hoặc null,
                                  "price": number hoặc null
                                }

                                Quy tắc:
                                - "dưới 100 nghìn" nghĩa là price <= 100000.
                                - "trên 50 nghìn" nghĩa là price >= 50000.
                                - keyword là tên món hoặc đặc điểm món.
                                - Nếu không có dữ liệu thì trả về null.
                                """;

                GroqRequest request = new GroqRequest(
                                model,
                                List.of(
                                                new GroqMessage("system", systemPrompt),
                                                new GroqMessage("user", userMessage)));

                GroqResponse response = restClient.post()
                                .uri(apiUrl)
                                .header("Authorization", "Bearer " + apiKey)
                                .header("Content-Type", "application/json")
                                .body(request)
                                .retrieve()
                                .body(GroqResponse.class);

                if (response == null
                                || response.choices() == null
                                || response.choices().isEmpty()
                                || response.choices().get(0).message() == null) {
                        throw new RuntimeException("Không thể phân tích yêu cầu người dùng");
                }

                String json = response.choices()
                                .get(0)
                                .message()
                                .content();

                try {
                        // objectMapper.readValue dùng để chuyển dữ liệu JSON thành Java Object.
                        return objectMapper.readValue(
                                        json,
                                        ProductSearchCriteria.class);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(
                                        "AI trả về JSON không hợp lệ: " + json,
                                        e);
                }
        }
}