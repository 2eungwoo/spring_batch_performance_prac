package study.batchperformance.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.batchperformance.domain.order.OrderStatus;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private BigDecimal amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @QueryProjection
    public OrderDto(Long id, BigDecimal amount, OrderStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
