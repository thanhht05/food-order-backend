package com.thanh.foodorder.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOSConfig {
    @Value("${payos.client-id}")
    private String clinetID;
    @Value("${payos.api-key}")
    private String apiKey;
    @Value("${payos.checksum-key}")
    private String checkSumKey;
    @Value("${payos.log-level}")
    private String logLevel;

    @Bean
    public PayOS payOS() {
        ClientOptions options = ClientOptions.builder()
                .clientId(clinetID)
                .apiKey(apiKey)
                .checksumKey(checkSumKey)
                .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
                .build();
        return new PayOS(options);
    }

}
