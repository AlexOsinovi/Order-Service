package by.osinovi.orderservice.integration;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.integration.config.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemControllerIntegrationTests extends BaseIntegrationTest {

    @Test
    void createItem_ValidRequest_ReturnsCreatedItem() {
        ItemRequestDto request = new ItemRequestDto("Test Item", new BigDecimal("19.99"));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", is(notNullValue()))
                .body("name", is("Test Item"))
                .body("price", is(19.99f));
    }

    @Test
    void createItem_InvalidRequest_ReturnsBadRequest() {
        ItemRequestDto request = new ItemRequestDto("", new BigDecimal("19.99"));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/items")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createItem_InvalidPrice_ReturnsBadRequest() {
        ItemRequestDto request = new ItemRequestDto("Test Item", new BigDecimal("-1.00"));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/items")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getItemById_ExistingItem_ReturnsItem() {
        Long itemId = createTestItem();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/items/" + itemId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(itemId.intValue()))
                .body("name", is("Test Item"))
                .body("price", is(19.99f));
    }

    @Test
    void getItemById_NonExistentItem_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getAllItems_ReturnsItemsList() {
        createTestItem();
        createTestItem();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/items")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    void updateItem_ValidRequest_UpdatesItem() {
        Long itemId = createTestItem();

        ItemRequestDto updateRequest = new ItemRequestDto("Updated Item", new BigDecimal("29.99"));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/items/" + itemId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(itemId.intValue()))
                .body("name", is("Updated Item"))
                .body("price", is(29.99f));
    }

    @Test
    void updateItem_NonExistentItem_ReturnsNotFound() {
        ItemRequestDto updateRequest = new ItemRequestDto("Updated Item", new BigDecimal("29.99"));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateItem_InvalidRequest_ReturnsBadRequest() {
        Long itemId = createTestItem();

        ItemRequestDto updateRequest = new ItemRequestDto("", new BigDecimal("29.99"));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/items/" + itemId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deleteItem_ExistingItem_DeletesItem() {
        Long itemId = createTestItem();

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/items/" + itemId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/items/" + itemId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteItem_NonExistentItem_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Long createTestItem() {
        ItemRequestDto request = new ItemRequestDto("Test Item", new BigDecimal("19.99"));

        return Long.valueOf(given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id").toString());
    }
} 