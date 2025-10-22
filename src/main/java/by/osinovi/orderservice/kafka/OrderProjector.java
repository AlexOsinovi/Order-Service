package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.document.OrderDocument;
import by.osinovi.orderservice.dto.message.OrderEvent;
import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.repository.OrderMongoRepository;
import by.osinovi.orderservice.service.UserInfoService;
import by.osinovi.orderservice.util.OrderStatus;
import by.osinovi.orderservice.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProjector {

    private final OrderMongoRepository mongoRepository;
    private final UserInfoService userInfoService;

    @KafkaListener(topics = "${spring.kafka.topics.order-events}", groupId = "order-projector-group")
    public void projectOrderEvent(OrderEvent event) {
        log.info("Projecting event for order ID: {}", event.getOrderId());
        
        UserInfoResponseDto userDto = userInfoService.getUserInfoById(event.getUserId());
        OrderDocument.UserInfo userInfo = new OrderDocument.UserInfo();
        userInfo.setId(userDto.getId());
        userInfo.setName(userDto.getName());
        userInfo.setSurname(userDto.getSurname());
        userInfo.setEmail(userDto.getEmail());

        List<OrderDocument.OrderItemInfo> itemInfos = event.getItems().stream()
                .map(itemData -> {
                    OrderDocument.OrderItemInfo itemInfo = new OrderDocument.OrderItemInfo();
                    itemInfo.setItemId(itemData.getItemId());
                    itemInfo.setItemName(itemData.getItemName());
                    itemInfo.setQuantity(itemData.getQuantity());
                    itemInfo.setItemPrice(itemData.getPrice());
                    return itemInfo;
                }).collect(Collectors.toList());

        OrderDocument orderDocument = new OrderDocument();
        orderDocument.setId(event.getOrderId());
        orderDocument.setStatus(event.getStatus());
        orderDocument.setCreationDate(event.getCreationDate());
        orderDocument.setTotalAmount(event.getTotalAmount());
        orderDocument.setUser(userInfo);
        orderDocument.setItems(itemInfos);

        mongoRepository.save(orderDocument);
        log.info("Saved projection for order ID: {}", event.getOrderId());
    }
    
    @KafkaListener(topics = "${spring.kafka.topics.payments}", groupId = "order-projector-group")
    public void projectPaymentStatusUpdate(PaymentMessage paymentMessage) {
        log.info("Projecting payment status for order ID: {}", paymentMessage.getOrderId());
        mongoRepository.findById(paymentMessage.getOrderId()).ifPresent(orderDoc -> {
            OrderStatus newStatus = paymentMessage.getStatus().equals(PaymentStatus.SUCCESS) 
                                    ? OrderStatus.PAID : OrderStatus.FAILED;
            orderDoc.setStatus(newStatus);
            orderDoc.setPaymentId(paymentMessage.getId());
            mongoRepository.save(orderDoc);
        });
        log.info("Updated order {} with status {}", paymentMessage.getOrderId(), paymentMessage.getStatus());
    }

    @KafkaListener(topics = "${spring.kafka.topics.order-deleted}", groupId = "order-projector-group")
    public void projectOrderDeletion(Long orderId) {
        log.info("Projecting deletion for order ID: {}", orderId);
        mongoRepository.deleteById(orderId);
    }
}