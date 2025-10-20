package study.batchperformance.batch.job.performance.repository;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.OrderJpaRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderProcessRepositoryJobConfig {

    @Value("${job.config.performance.repository.name}")
    private String jobName;

    @Value("${job.config.performance.repository.step}")
    private String stepName;

    @Value("${job.config.performance.repository.chunk-size}")
    private int chunkSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public Job orderProcessRepositoryJob() {
        return new JobBuilder(jobName, jobRepository)
                .start(orderProcessRepositoryStep())
                .build();
    }

    @Bean
    public Step orderProcessRepositoryStep() {
        return new StepBuilder(stepName, jobRepository)
                .<OrderEntity, OrderEntity>chunk(chunkSize, transactionManager)
                .reader(orderProcessRepositoryReader())
                .processor(orderProcessRepositoryProcessor())
                .writer(orderProcessRepositoryWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<OrderEntity> orderProcessRepositoryReader() {
        return new RepositoryItemReaderBuilder<OrderEntity>()
                .name("orderProcessRepositoryReader")
                .repository(orderJpaRepository)
                .methodName("findByStatus")
                .arguments(List.of(OrderStatus.CREATED))
                .pageSize(chunkSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> orderProcessRepositoryProcessor() {
        return source -> {
            source.updateStatus(OrderStatus.PROCESSING);
            return source;
        };
    }

    @Bean
    public ItemWriter<OrderEntity> orderProcessRepositoryWriter() {
        return orderJpaRepository::saveAll;
    }
}
