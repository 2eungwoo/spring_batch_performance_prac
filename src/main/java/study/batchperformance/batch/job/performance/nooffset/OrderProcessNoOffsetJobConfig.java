package study.batchperformance.batch.job.performance.nooffset;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.batch.job.performance.nooffset.reader.QueryDslNoOffsetItemReader;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.OrderJpaRepository;

import static study.batchperformance.domain.order.QOrderEntity.orderEntity;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderProcessNoOffsetJobConfig {

    @Value("${job.config.no-offset.name}")
    private String jobName;

    @Value("${job.config.no-offset.step}")
    private String stepName;

    @Value("${job.config.no-offset.chunk-size}")
    private int chunkSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final OrderJpaRepository orderJpaRepository;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManagerFactory.createEntityManager());
    }

    @Bean
    public Job orderProcessNoOffsetJob() {
        return new JobBuilder(jobName, jobRepository)
                .start(orderProcessNoOffsetStep())
                .build();
    }

    @Bean
    @JobScope
    public Step orderProcessNoOffsetStep() {
        return new StepBuilder(stepName, jobRepository)
                .<OrderEntity, OrderEntity>chunk(chunkSize, transactionManager)
                .reader(queryDslNoOffsetReader())
                .processor(orderProcessNoOffsetProcessor())
                .writer(orderProcessNoOffsetWriter())
                .build();
    }

    @Bean
    public QueryDslNoOffsetItemReader<OrderEntity> queryDslNoOffsetReader() {
        return new QueryDslNoOffsetItemReader<>(
                jpaQueryFactory(),
                chunkSize,
                // ID 경로와 ID 추출 방법을 Reader에 전달
                orderEntity.id,
                OrderEntity::getId,
                queryFactory -> queryFactory
                        .selectFrom(orderEntity)
                        .where(orderEntity.status.eq(OrderStatus.CREATED))
        );
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> orderProcessNoOffsetProcessor() {
        return source -> {
            source.updateStatus(OrderStatus.PROCESSING);
            return source;
        };
    }

    @Bean
    public ItemWriter<OrderEntity> orderProcessNoOffsetWriter() {
        return orderJpaRepository::saveAll;
    }
}
