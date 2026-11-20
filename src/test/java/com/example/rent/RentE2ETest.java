package com.example.rent;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RentE2ETest {

    private static Long customerId;
    private static Long equipmentId;
    private static Long rentalId;

    @BeforeAll
    static void setup() {
        String host = System.getenv("DEPLOY_HOST");
        RestAssured.baseURI = (host != null) ? "http://" + host + ":8080" : "http://localhost:8080";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    void createCustomer() {
        String body = "{\"email\":\"e2e_" + System.currentTimeMillis() + "@mail.ru\",\"fullName\":\"E2E Тест\",\"phone\":\"+79991112233\"}";

        customerId = given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/api/customers")
        .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    void createEquipment() {
        String eqBody = "{\"name\":\"E2E Камера\",\"category\":\"Фото\",\"dailyPrice\":150}";

        equipmentId = given()
                .contentType(ContentType.JSON)
                .body(eqBody)
        .when()
                .post("/api/equipment")
        .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void createRental() {
        String rentalBody = "{\"customerId\":" + customerId + ",\"equipmentId\":" + equipmentId + ",\"startDate\":\"" + LocalDate.now().plusDays(1) + "\",\"endDate\":\"" + LocalDate.now().plusDays(3) + "\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(rentalBody)
        .when()
                .post("/api/rentals");

        response.then().statusCode(201);
        rentalId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(4)
    void readRental() {
        if (rentalId == null) throw new RuntimeException("rentalId is null!");

        given()
                .pathParam("id", rentalId) // ИСПОЛЬЗУЕМ ПАРАМЕТР ВМЕСТО СКЛЕЙКИ СТРОК
        .when()
                .get("/api/rentals/{id}")
        .then()
                .statusCode(200)
                .body("id", equalTo(rentalId.intValue()));
    }

    @Test
    @Order(5)
    void cancelRental() {
        if (rentalId == null) throw new RuntimeException("rentalId is null!");

        given()
                .pathParam("id", rentalId)
        .when()
                .delete("/api/rentals/{id}")
        .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}