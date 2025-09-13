package by.osinovi.orderservice.integration;

import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.repository.OrderItemRepository;
import by.osinovi.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderControllerIntegrationTests {

    @LocalServerPort
    private Integer port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private by.osinovi.orderservice.kafka.OrderProducer orderProducer;

    private static WireMockServer wireMockServer;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_service_test")
            .withUsername("postgres")
            .withPassword("password");

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        wireMockServer.resetAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        doNothing().when(orderProducer).sendCreateOrderEvent(any());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("user.service.url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.topics.orders", () -> "orders-topic");
        registry.add("spring.kafka.topics.payments", () -> "payments-topic");
        registry.add("spring.kafka.consumer.group-id", () -> "order-service-group");
    }

    @Test
    void createOrder_ValidRequest_ReturnsCreatedOrder() {
        Item item = createTestItem();

        String userResponse = """
        {
            "id": 100,
            "name": "John",
            "surname": "Doe",
            "birthDate": "1990-01-01",
            "email": "john@example.com"
        }
        """;

        wireMockServer.stubFor(
                get(urlEqualTo("/100"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userResponse))
        );

        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 2);
        OrderRequestDto request = new OrderRequestDto(100L, LocalDate.now(), List.of(itemReq));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("order.id", is(notNullValue()))
                .body("order.userId", is(100))
                .body("order.status", is("CREATED"))
                .body("user.id", is(100))
                .body("user.name", is("John"));
    }

    @Test
    void createOrder_UserNotFound_ReturnsNotFound() {
        Item item = createTestItem();

        wireMockServer.stubFor(
                get(urlEqualTo("/999"))
                        .willReturn(aResponse().withStatus(404))
        );

        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 1);
        OrderRequestDto request = new OrderRequestDto(999L, LocalDate.now(), List.of(itemReq));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() {
        Long orderId = createTestOrder();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders/" + orderId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("order.id", is(orderId.intValue()))
                .body("order.status", is("CREATED"));
    }

    @Test
    void getOrderById_NonExistentOrder_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getAllOrders_ReturnsOrdersList() {
        String userResponse200 = """
        {
            "id": 200,
            "name": "Jane",
            "surname": "Smith",
            "birthDate": "1995-05-15",
            "email": "jane@example.com"
        }
        """;
        wireMockServer.stubFor(
                get(urlEqualTo("/200"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userResponse200))
        );

        Item item = createTestItem();
        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 2);
        OrderRequestDto request = new OrderRequestDto(200L, LocalDate.now(), List.of(itemReq));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("order.id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThan(0));
    }

    @Test
    void getOrdersByStatuses_ValidStatuses_ReturnsFilteredOrders() {
        String userResponse200 = """
        {
            "id": 200,
            "name": "Jane",
            "surname": "Smith",
            "birthDate": "1995-05-15",
            "email": "jane@example.com"
        }
        """;
        wireMockServer.stubFor(
                get(urlEqualTo("/200"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userResponse200))
        );

        Item item1 = createTestItem();
        OrderItemRequestDto itemReq1 = new OrderItemRequestDto(item1.getId(), 2);
        OrderRequestDto request1 = new OrderRequestDto(200L, LocalDate.now(), List.of(itemReq1));
        given()
                .contentType(ContentType.JSON)
                .body(request1)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        Item item2 = createTestItem();
        OrderItemRequestDto itemReq2 = new OrderItemRequestDto(item2.getId(), 1);
        OrderRequestDto request2 = new OrderRequestDto(200L, LocalDate.now(), List.of(itemReq2));

        given()
                .contentType(ContentType.JSON)
                .body(request2)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders/statuses?statuses=CREATED&statuses=PAID")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThan(0))
                .body("[0].order.status", anyOf(is("CREATED"), is("PAID")));
    }

    @Test
    void getOrdersByStatuses_EmptyStatuses_ReturnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("statuses")
                .when()
                .get("/api/orders/statuses")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateOrder_ValidRequest_UpdatesOrder() {
        Long orderId = createTestOrder();

        String userResponse = """
        {
            "id": 200,
            "name": "Jane",
            "surname": "Smith",
            "birthDate": "1995-05-15",
            "email": "jane@example.com"
        }
        """;

        wireMockServer.stubFor(
                get(urlEqualTo("/200"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userResponse))
        );

        Item item = createTestItem();
        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 3);
        OrderRequestDto updateRequest = new OrderRequestDto(200L, LocalDate.now(), List.of(itemReq));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/orders/" + orderId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("order.id", is(orderId.intValue()))
                .body("order.status", is("CHANGED"))
                .body("user.id", is(200));
    }

    @Test
    void updateOrder_NonExistentOrder_ReturnsNotFound() {
        Item item = createTestItem();
        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 1);
        OrderRequestDto updateRequest = new OrderRequestDto(1L, LocalDate.now(), List.of(itemReq));

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/orders/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteOrder_ExistingOrder_DeletesOrder() {
        Long orderId = createTestOrder();

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/orders/" + orderId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders/" + orderId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteOrder_NonExistentOrder_ReturnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/orders/99999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Item createTestItem() {
        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(new BigDecimal("19.99"));
        return itemRepository.save(item);
    }

    private Long createTestOrder() {
        Item item = createTestItem();

        String userResponse = """
        {
            "id": 100,
            "name": "John",
            "surname": "Doe",
            "birthDate": "1990-01-01",
            "email": "john@example.com"
        }
        """;

        wireMockServer.stubFor(
                get(urlEqualTo("/100"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userResponse))
        );

        OrderItemRequestDto itemReq = new OrderItemRequestDto(item.getId(), 2);
        OrderRequestDto request = new OrderRequestDto(100L, LocalDate.now(), List.of(itemReq));

        return Long.valueOf(given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("order.id").toString());
    }
}