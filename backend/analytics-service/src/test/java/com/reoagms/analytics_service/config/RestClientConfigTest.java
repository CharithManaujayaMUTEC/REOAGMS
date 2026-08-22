package com.reoagms.analytics_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {

    private final RestClientConfig config = new RestClientConfig();

    @Test
    void createsAllDownstreamRestClientsWithoutAnInjectedBuilder() {
        JdkClientHttpRequestFactory requestFactory = config.analyticsRequestFactory(3, 10);

        RestClient monitoring = config.monitoringRestClient(requestFactory, "http://localhost:8083");
        RestClient asset = config.assetRestClient(requestFactory, "http://localhost:8082");
        RestClient maintenance = config.maintenanceRestClient(requestFactory, "http://localhost:8084");

        assertThat(monitoring).isNotNull();
        assertThat(asset).isNotNull();
        assertThat(maintenance).isNotNull();
    }
}
