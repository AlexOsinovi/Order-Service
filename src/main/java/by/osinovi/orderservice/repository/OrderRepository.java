package by.osinovi.orderservice.repository;

import by.osinovi.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllById(Iterable<Long> ids);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatuses(@Param("statuses") List<String> statuses);
}