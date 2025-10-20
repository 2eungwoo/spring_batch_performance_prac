package study.batchperformance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.projection.OrderStatisticsProjection;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByStatus(OrderStatus status);

    @Query("""
            select o.status as status,
                   count(o) as orderCount,
                   coalesce(sum(o.amount), 0) as totalAmount
            from OrderEntity o
            group by o.status
            """)
    List<OrderStatisticsProjection> aggregateByStatus();
}
