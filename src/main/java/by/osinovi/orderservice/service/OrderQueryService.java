package by.osinovi.orderservice.service;

import by.osinovi.orderservice.document.OrderDocument;

import java.util.List;

public interface OrderQueryService {
    OrderDocument getOrderById(Long id);

    List<OrderDocument> getAllOrders();


}
