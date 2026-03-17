package by.osinovi.orderservice.repository;

import by.osinovi.orderservice.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderMongoRepository extends MongoRepository<OrderDocument, Long> {
    List<OrderDocument> findByUser_Id(Long userId);
}