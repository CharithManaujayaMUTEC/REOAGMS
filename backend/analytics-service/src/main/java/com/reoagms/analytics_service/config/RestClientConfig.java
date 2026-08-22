package com.reoagms.analytics_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    JdkClientHttpRequestFactory analyticsRequestFactory(
            @Value("${integration.connect-timeout-seconds:3}") long connectTimeout,
            @Value("${integration.read-timeout-seconds:10}") long readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(readTimeout));
        return factory;
    }

    @Bean("monitoringRestClient")
    RestClient monitoringRestClient(
            JdkClientHttpRequestFactory analyticsRequestFactory,
            @Value("${integration.monitoring-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(analyticsRequestFactory)
                .build();
    }

    @Bean("assetRestClient")
    RestClient assetRestClient(
            JdkClientHttpRequestFactory analyticsRequestFactory,
            @Value("${integration.asset-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(analyticsRequestFactory)
                .build();
    }

    @Bean("maintenanceRestClient")
    RestClient maintenanceRestClient(
            JdkClientHttpRequestFactory analyticsRequestFactory,
            @Value("${integration.maintenance-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(analyticsRequestFactory)
                .build();
    }
}
