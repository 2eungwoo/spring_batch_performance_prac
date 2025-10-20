package study.batchperformance.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.domain.statistics.OrderStatisticsEntity;

public interface OrderStatisticsRepository extends JpaRepository<OrderStatisticsEntity, Long> {

    Optional<OrderStatisticsEntity> findByStatus(OrderStatus status);
}
