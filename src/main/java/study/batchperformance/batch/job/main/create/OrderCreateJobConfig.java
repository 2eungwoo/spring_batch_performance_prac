package study.batchperformance.batch.job.main.create;

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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${job.config.create.name}")
    private String jobName;

    @Value("${job.config.create.step}")
    private String stepName;

    @Value("${job.config.create.chunk-size}")
    private int chunkSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public Job orderCreateJob() {
        return new JobBuilder(jobName, jobRepository)
                .start(orderCreateStep())
                .build();
    }

    @Bean
    public Step orderCreateStep() {
        return new StepBuilder(stepName, jobRepository)
                .<OrderEntity, OrderEntity>chunk(chunkSize, transactionManager)
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
                .pageSize(chunkSize)
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
