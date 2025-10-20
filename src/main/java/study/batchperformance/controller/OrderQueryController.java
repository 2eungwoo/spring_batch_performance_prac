package study.batchperformance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.statistics.OrderStatisticsEntity;
import study.batchperformance.repository.OrderJpaRepository;
import study.batchperformance.repository.OrderStatisticsRepository;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderStatisticsRepository orderStatisticsRepository;

    /*
     * 예시: /api/orders?page=0&size=20&sort=id,desc
     */
    @GetMapping("/orders")
    public Page<OrderEntity> fetchOrders(Pageable pageable) {
        return orderJpaRepository.findAll(pageable);
    }

    /*
     * 예시: /api/order-statistics?page=0&size=10
     */
    @GetMapping("/order-statistics")
    public Page<OrderStatisticsEntity> fetchOrderStatistics(Pageable pageable) {
        return orderStatisticsRepository.findAll(pageable);
    }
}