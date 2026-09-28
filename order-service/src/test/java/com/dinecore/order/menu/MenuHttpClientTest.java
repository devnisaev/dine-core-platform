package com.dinecore.order.menu;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MenuHttpClientTest {

    @Test
    void priceIncludesSelectedModifiers() {
        UUID branchId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        UUID modifierId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://menu/api/v1/menu"))
                .andExpect(header("X-Tenant-ID", branchId.toString()))
                .andRespond(withSuccess(menu(branchId, dishId, modifierId), MediaType.APPLICATION_JSON));
        MenuHttpClient client = new MenuHttpClient(builder.baseUrl("http://menu").build(), JsonMapper.builder().build());

        PricedDish priced = client.price(branchId, dishId, List.of(modifierId), "Bearer token");

        assertThat(priced.unitPrice()).isEqualByComparingTo("500.00");
        assertThat(priced.name()).isEqualTo("Lagman");
        assertThat(priced.modifiersJson()).contains("meat");
        server.verify();
    }

    private static String menu(UUID branchId, UUID dishId, UUID modifierId) {
        return """
                {"branchId":"%s","categories":[{"id":"%s","name":"Hot","sortOrder":1,"dishes":[
                {"id":"%s","name":"Lagman","description":"soup","price":450.00,"modifiers":[
                {"id":"%s","name":"meat","priceDelta":50.00}]}]}]}
                """.formatted(branchId, UUID.randomUUID(), dishId, modifierId);
    }
}
