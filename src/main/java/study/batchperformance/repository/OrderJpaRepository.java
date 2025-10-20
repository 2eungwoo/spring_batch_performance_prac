package study.batchperformance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.batchperformance.domain.order.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}
