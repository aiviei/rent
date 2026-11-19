package com.example.rent;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.Map;

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
        Map<String, String> body = Map.of(
                "email", "e2e_" + System.currentTimeMillis() + "@mail.ru",
                "fullName", "E2E Тест",
                "phone", "+79991112233"
        );

        customerId = given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/api/customers")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    void createEquipmentAndRental() {
        Map<String, Object> eq = Map.of(
                "name", "E2E Камера",
                "category", "Фото",
                "dailyPrice", 150,
                "status", "available"
        );

        equipmentId = given()
                .contentType(ContentType.JSON)
                .body(eq)
        .when()
                .post("/api/equipment")
        .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        Map<String, Object> rental = Map.of(
                "customerId", customerId,
                "equipmentId", equipmentId,
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusDays(3).toString()
        );

        rentalId = given()
                .contentType(ContentType.JSON)
                .body(rental)
        .when()
                .post("/api/rentals")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void readRental() {
        given()
        .when()
                .get("/api/rentals/" + rentalId)
        .then()
                .statusCode(200)
                .body("id", equalTo(rentalId.intValue()));
    }

    @Test
    @Order(4)
    void cancelRental() {
        given()
        .when()
                .delete("/api/rentals/" + rentalId)
        .then()
                .statusCode(anyOf(is(200), is(204)));
    }
}