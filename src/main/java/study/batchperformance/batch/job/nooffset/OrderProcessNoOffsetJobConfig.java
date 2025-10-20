package study.batchperformance.batch.job.nooffset;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.batch.job.nooffset.reader.QueryDslNoOffsetItemReader;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.repository.OrderJpaRepository;

import static study.batchperformance.domain.order.QOrderEntity.orderEntity;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderProcessNoOffsetJobConfig {

    private static final String JOB_NAME = "orderProcessNoOffsetJob";
    private static final String STEP_NAME = "orderProcessNoOffsetStep";
    private static final int CHUNK_SIZE = 1000;

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
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(orderProcessNoOffsetStep())
                .build();
    }

    @Bean
    @JobScope
    public Step orderProcessNoOffsetStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<OrderEntity, OrderEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(queryDslNoOffsetReader())
                .processor(orderProcessNoOffsetProcessor())
                .writer(orderProcessNoOffsetWriter())
                .build();
    }

    @Bean
    public QueryDslNoOffsetItemReader<OrderEntity> queryDslNoOffsetReader() {
        return new QueryDslNoOffsetItemReader<>(
                jpaQueryFactory(),
                CHUNK_SIZE,
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
