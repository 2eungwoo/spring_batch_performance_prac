package study.batchperformance.batch;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.repository.OrderJpaRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderJobConfig {

    private static final String JOB_NAME = "orderJob";
    private static final String STEP_NAME = "orderLoggingStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public Job orderJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(orderLoggingStep())
                .build();
    }

    @Bean
    public Step orderLoggingStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<OrderEntity, OrderEntity>chunk(5, transactionManager)
                .reader(orderReader())
                .writer(orderWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<OrderEntity> orderReader() {
        return new RepositoryItemReaderBuilder<OrderEntity>()
                .name("orderReader")
                .repository(orderJpaRepository)
                .methodName("findAll")
                .pageSize(5)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<OrderEntity> orderWriter() {
        return items -> items.forEach(order ->
                log.info("주문 금액: {}, 상태: {}", order.getAmount(), order.getStatus())
        );
    }
}
