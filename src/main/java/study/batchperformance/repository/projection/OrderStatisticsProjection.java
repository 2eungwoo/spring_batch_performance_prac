package study.batchperformance.repository.projection;

import java.math.BigDecimal;
import study.batchperformance.domain.order.OrderStatus;

public interface OrderStatisticsProjection {

    OrderStatus getStatus();

    Long getOrderCount();

    BigDecimal getTotalAmount();
}
