package study.batchperformance.domain.statistics;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.global.model.BaseTimeEntity;

@Getter
@Entity
@Table(name = "order_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatisticsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long orderCount;

    private BigDecimal totalAmount;

    private OrderStatisticsEntity(OrderStatus status, Long orderCount, BigDecimal totalAmount) {
        this.status = status;
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
    }

    public static OrderStatisticsEntity of(OrderStatus status, Long orderCount, BigDecimal totalAmount) {
        return new OrderStatisticsEntity(status, orderCount, totalAmount);
    }

    public void refresh(Long orderCount, BigDecimal totalAmount) {
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
    }
}
