package study.batchperformance.util.init;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.OrderJpaRepository;

@Component
@RequiredArgsConstructor
public class OrderDataInitializer {

    private final OrderJpaRepository orderJpaRepository;

    @PostConstruct
    public void loadSampleData() {
        if (orderJpaRepository.count() > 0) {
            return;
        }

        List<OrderEntity> samples = List.of(
                OrderEntity.builder()
                        .amount(BigDecimal.valueOf(1000))
                        .status(OrderStatus.CREATED)
                        .build(),
                OrderEntity.builder()
                        .amount(BigDecimal.valueOf(2000))
                        .status(OrderStatus.PROCESSING)
                        .build(),
                OrderEntity.builder()
                        .amount(BigDecimal.valueOf(3000))
                        .status(OrderStatus.COMPLETED)
                        .build()
        );

        orderJpaRepository.saveAll(samples);
    }
}
