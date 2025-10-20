package study.batchperformance.batch.job.statistics;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.statistics.OrderStatisticsEntity;
import study.batchperformance.repository.OrderJpaRepository;
import study.batchperformance.repository.OrderStatisticsRepository;
import study.batchperformance.repository.projection.OrderStatisticsProjection;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStatisticsJobConfig {

    private static final String JOB_NAME = "orderStatisticsJob";
    private static final String STEP_NAME = "orderStatisticsStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderJpaRepository orderJpaRepository;
    private final OrderStatisticsRepository orderStatisticsRepository;

    @Bean
    public Job orderStatisticsJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(orderStatisticsStep())
                .build();
    }

    @Bean
    public Step orderStatisticsStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<OrderStatisticsProjection, OrderStatisticsEntity>chunk(10, transactionManager)
                .reader(orderStatisticsReader())
                .processor(orderStatisticsProcessor())
                .writer(orderStatisticsWriter())
                .build();
    }

    @Bean
    public ListItemReader<OrderStatisticsProjection> orderStatisticsReader() {
        List<OrderStatisticsProjection> aggregated = orderJpaRepository.aggregateByStatus();
        return new ListItemReader<>(aggregated);
    }

    @Bean
    public ItemProcessor<OrderStatisticsProjection, OrderStatisticsEntity> orderStatisticsProcessor() {
        return projection -> OrderStatisticsEntity.of(
                projection.getStatus(),
                projection.getOrderCount(),
                projection.getTotalAmount()
        );
    }

    @Bean
    public ItemWriter<OrderStatisticsEntity> orderStatisticsWriter() {
        return items -> {
            items.forEach(item -> orderStatisticsRepository.findByStatus(item.getStatus())
                    .ifPresentOrElse(existing -> existing.refresh(item.getOrderCount(), item.getTotalAmount()),
                            () -> orderStatisticsRepository.save(item)));
            log.info("주문 통계 {}건 집계 완료", items.size());
        };
    }
}
