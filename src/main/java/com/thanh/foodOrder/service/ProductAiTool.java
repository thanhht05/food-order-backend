package com.thanh.foodorder.service;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.dto.request.ProductSuggestion;
import com.thanh.foodorder.repository.ProductRepository;

@Component
public class ProductAiTool {

    private final ProductRepository productRepository;

    public ProductAiTool(ProductRepository productrRepository) {
        this.productRepository = productrRepository;
    }

    @Tool(description = "Search available food products by customer request")
    public List<ProductSuggestion> searchProducts(String keyword) {

        System.out.println("AI keyword = [" + keyword + "]");

        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword.trim());

        System.out.println("Products found = " + products.size());

        products.forEach(product -> System.out.println(
                "Found product: [" + product.getName() + "]"));

        return products.stream()
                .limit(5)
                .map(product -> new ProductSuggestion(
                        product.getId(),
                        product.getName(),
                        product.getPrice()))
                .toList();
    }
}