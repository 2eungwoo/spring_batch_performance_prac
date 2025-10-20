package study.batchperformance.batch.job.update;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.OrderJpaRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderUpdateJobConfig {

    private static final String JOB_NAME = "orderUpdateJob";
    private static final String STEP_NAME = "orderUpdateStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public Job orderUpdateJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(orderUpdateStep())
                .build();
    }

    @Bean
    public Step orderUpdateStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<OrderEntity, OrderEntity>chunk(1000, transactionManager)
                .reader(orderUpdateReader())
                .processor(orderUpdateProcessor())
                .writer(orderUpdateWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<OrderEntity> orderUpdateReader() {
        return new RepositoryItemReaderBuilder<OrderEntity>()
                .name("orderUpdateReader")
                .repository(orderJpaRepository)
                .methodName("findByStatus")
                .arguments(List.of(OrderStatus.PROCESSING))
                .pageSize(1000)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> orderUpdateProcessor() {
        return order -> {
            log.debug("주문 업데이트 대상: {}", order.getId());
            order.updateStatus(OrderStatus.COMPLETED);
            return order;
        };
    }

    @Bean
    public ItemWriter<OrderEntity> orderUpdateWriter() {
        return items -> {
            orderJpaRepository.saveAll(items);
            log.info("주문 {}건 상태 업데이트 완료", items.size());
        };
    }
}
