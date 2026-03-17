package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.document.OrderDocument;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.repository.OrderMongoRepository;
import by.osinovi.orderservice.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderMongoRepository mongoRepository;

    @Override
    public OrderDocument getOrderById(Long id) {
        return mongoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with " + id + " not found in read model"));
    }

    @Override
    public List<OrderDocument> getAllOrders() {
        return mongoRepository.findAll();
    }
    }