package by.osinovi.orderservice.integration;

import by.osinovi.orderservice.dto.orderItem.OrderItemRequestDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.repository.OrderRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderItemControllerIntegrationTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_service_test")
            .withUsername("postgres")
            .withPassword("password");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Test
    void createOrderItem_ValidRequest_ReturnsCreatedOrderItem() {
        Order order = createTestOrder();
        Item item = createTestItem();

        OrderItemRequestDto request = new OrderItemRequestDto(item.getId(), 3);

        given()
                .contentType(ContentType.JSON)
                .queryParam("orderId", order.getId())
                .body(request)
                .when()
                .post("/api/order-items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", is(notNullValue()))
                .body("quantity", is(3));
    }

    @Test
    void createOrderItem_NonExistentOrder_ReturnsNotFound() {
        Item item = createTestItem();
        OrderItemRequestDto request = new OrderItemRequestDto(item.getId(), 2);

        given()
                .contentType(ContentType.JSON)
                .queryParam("orderId", 99999L)
                .body(request)
                .when()
                .post("/api/order-items")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void createOrderItem_InvalidRequest_ReturnsBadRequest() {
        Order order = createTestOrder();
        OrderItemRequestDto request = new OrderItemRequestDto(null, 0);

        given()
                .contentType(ContentType.JSON)
                .queryParam("orderId", order.getId())
                .body(request)
                .when()
                .post("/api/order-items")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getOrderItemById_ExistingOrderItem_ReturnsOrderItem() {
        Long orderItemId = createTestOrderItem();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/order-items/" + orderItemId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(orderItemId.intValue()))
                .body("quantity", is(2));
    }

    @Test
    void getOrderItemById_NonExistentOrderItem_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/order-items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getOrderItemsByOrderId_ReturnsOrderItemsList() {
        Order order = createTestOrder();
        createTestOrderItemForOrder(order.getId());
        createTestOrderItemForOrder(order.getId());

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/order-items/order/" + order.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    void getOrderItemsByOrderId_NonExistentOrder_ReturnsEmptyList() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/order-items/order/99999")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    void updateOrderItem_ValidRequest_UpdatesOrderItem() {
        Long orderItemId = createTestOrderItem();

        OrderItemRequestDto updateRequest = new OrderItemRequestDto(1L, 5);

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/order-items/" + orderItemId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", is(orderItemId.intValue()))
                .body("quantity", is(5));
    }

    @Test
    void updateOrderItem_NonExistentOrderItem_ReturnsNotFound() {
        OrderItemRequestDto updateRequest = new OrderItemRequestDto(1L, 5);

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/order-items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateOrderItem_InvalidRequest_ReturnsBadRequest() {
        Long orderItemId = createTestOrderItem();

        OrderItemRequestDto updateRequest = new OrderItemRequestDto(null, 0);

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/order-items/" + orderItemId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deleteOrderItem_ExistingOrderItem_DeletesOrderItem() {
        Long orderItemId = createTestOrderItem();

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/order-items/" + orderItemId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/order-items/" + orderItemId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteOrderItem_NonExistentOrderItem_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/order-items/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Item createTestItem() {
        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(new BigDecimal("19.99"));
        return itemRepository.save(item);
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setUserId(100L);
        order.setStatus("NEW");
        order.setCreationDate(LocalDate.now());
        return orderRepository.save(order);
    }

    private Long createTestOrderItem() {
        Order order = createTestOrder();
        return createTestOrderItemForOrder(order.getId());
    }

    private Long createTestOrderItemForOrder(Long orderId) {
        Item item = createTestItem();
        OrderItemRequestDto request = new OrderItemRequestDto(item.getId(), 2);

        return Long.valueOf(given()
                .contentType(ContentType.JSON)
                .queryParam("orderId", orderId)
                .body(request)
                .when()
                .post("/api/order-items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id").toString());
    }
} 