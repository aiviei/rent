package com.example.rent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RentE2ETest {

    private static String baseUrl;
    private static HttpClient client;
    
    private static Long customerId;
    private static Long equipmentId;
    private static Long rentalId;

    @BeforeAll
    static void setup() {
        String host = System.getenv("DEPLOY_HOST");
        baseUrl = (host != null) ? "http://" + host + ":8080" : "http://localhost:8080";
        client = HttpClient.newHttpClient();
    }

    private HttpResponse<String> sendRequest(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json");

        if (body != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Long extractId(String json) {
        String idStr = json.split("\"id\":")[1].split(",")[0].replaceAll("[^0-9]", "");
        return Long.parseLong(idStr);
    }

    @Test
    @Order(1)
    void createCustomer() throws Exception {
        String body = "{\"email\":\"e2e_" + System.currentTimeMillis() + "@mail.ru\",\"fullName\":\"E2E Тест\",\"phone\":\"+79991112233\"}";
        HttpResponse<String> response = sendRequest("POST", "/api/customers", body);
        
        assertTrue(response.statusCode() == 201, "Ожидался статус 201, получен " + response.statusCode() + ". Ответ: " + response.body());
        customerId = extractId(response.body());
    }

    @Test
    @Order(2)
    void createEquipment() throws Exception {
        String body = "{\"name\":\"E2E Камера\",\"category\":\"Фото\",\"dailyPrice\":150}";
        HttpResponse<String> response = sendRequest("POST", "/api/equipment", body);
        
        assertTrue(response.statusCode() == 201, "Ожидался статус 201, получен " + response.statusCode() + ". Ответ: " + response.body());
        equipmentId = extractId(response.body());
    }

    @Test
    @Order(3)
    void createRental() throws Exception {
        String body = "{\"customerId\":" + customerId + ",\"equipmentId\":" + equipmentId + ",\"startDate\":\"" + LocalDate.now().plusDays(1) + "\",\"endDate\":\"" + LocalDate.now().plusDays(3) + "\"}";
        HttpResponse<String> response = sendRequest("POST", "/api/rentals", body);
        
        assertTrue(response.statusCode() == 201, "Ожидался статус 201, получен " + response.statusCode() + ". Ответ: " + response.body());
        rentalId = extractId(response.body());
    }

    @Test
    @Order(4)
    void readRental() throws Exception {
        if (rentalId == null) throw new RuntimeException("rentalId is null!");
        
        HttpResponse<String> response = sendRequest("GET", "/api/rentals/" + rentalId, null);
        assertTrue(response.statusCode() == 200, "Ожидался статус 200, получен " + response.statusCode());
    }

    @Test
    @Order(5)
    void cancelRental() throws Exception {
        if (rentalId == null) throw new RuntimeException("rentalId is null!");
        
        HttpResponse<String> response = sendRequest("PATCH", "/api/rentals/" + rentalId + "/cancel", null);
        
        assertTrue(response.statusCode() == 204 || response.statusCode() == 200, 
            "Ожидался статус 200/204, получен " + response.statusCode());
    }
}