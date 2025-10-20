package study.batchperformance.batch.job.create;

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
public class OrderCreateJobConfig {

    private static final String JOB_NAME = "orderCreateJob";
    private static final String STEP_NAME = "orderCreateStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public Job orderCreateJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(orderCreateStep())
                .build();
    }

    @Bean
    public Step orderCreateStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<OrderEntity, OrderEntity>chunk(10, transactionManager)
                .reader(orderCreateReader())
                .processor(orderCreateProcessor())
                .writer(orderCreateWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<OrderEntity> orderCreateReader() {
        return new RepositoryItemReaderBuilder<OrderEntity>()
                .name("orderCreateReader")
                .repository(orderJpaRepository)
                .methodName("findByStatus")
                .arguments(List.of(OrderStatus.CREATED))
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> orderCreateProcessor() {
        return source -> {
            log.debug("신규 주문 생성 대상: {}", source.getId());
            return OrderEntity.builder()
                    .amount(source.getAmount())
                    .status(OrderStatus.PROCESSING)
                    .build();
        };
    }

    @Bean
    public ItemWriter<OrderEntity> orderCreateWriter() {
        return items -> {
            orderJpaRepository.saveAll(items);
            log.info("신규 주문 {}건 생성 완료", items.size());
        };
    }
}
