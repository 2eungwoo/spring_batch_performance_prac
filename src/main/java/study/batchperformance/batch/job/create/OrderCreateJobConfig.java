package study.batchperformance.batch.job.create;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private static final int CHUNK_SIZE = 1000;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory; // EntityManagerFactory 주입
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
                .<OrderEntity, OrderEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(orderCreateReader())
                .processor(orderCreateProcessor())
                .writer(orderCreateWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<OrderEntity> orderCreateReader() {
        String jpqlQuery = "SELECT o FROM OrderEntity o WHERE o.status = :status ORDER BY o.id ASC";

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("status", OrderStatus.CREATED);

        return new JpaPagingItemReaderBuilder<OrderEntity>()
                .name("orderCreateReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(jpqlQuery)
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> orderCreateProcessor() {
        return source -> {
            // source 객체의 상태를 직접 변경하여 업데이트
            log.debug("신규 주문 처리 대상: {}", source.getId());
            source.updateStatus(OrderStatus.PROCESSING);
            return source;
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
