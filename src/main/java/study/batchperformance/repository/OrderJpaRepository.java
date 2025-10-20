package study.batchperformance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.projection.OrderStatisticsProjection;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
            select o.status as status,
                   count(o) as orderCount,
                   coalesce(sum(o.amount), 0) as totalAmount
            from OrderEntity o
            group by o.status
            """)
    List<OrderStatisticsProjection> aggregateByStatus();
}
