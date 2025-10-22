package study.batchperformance.batch.job.performance.jdbc_batch_writer.create;

import static study.batchperformance.domain.order.QOrderEntity.orderEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.order.OrderStatus;
import study.batchperformance.dto.OrderDto;
import study.batchperformance.dto.QOrderDto;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderCreateJobConfigV4 {

    @Value("${job.config.create.name}-v4")
    private String jobName;

    @Value("${job.config.create.step}-v4")
    private String stepName;

    @Value("${job.config.create.chunk-size}")
    private int chunkSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManagerFactory.createEntityManager());
    }

    @Bean
    public Job orderCreateJobV4() {
        return new JobBuilder(jobName, jobRepository)
            .start(orderCreateStepV4())
            .build();
    }

    @Bean
    public Step orderCreateStepV4() {
        return new StepBuilder(stepName, jobRepository)
            .<OrderDto, OrderDto>chunk(chunkSize, transactionManager)
            .reader(orderCreateReaderV4())
            .processor(orderCreateProcessorV4())
            .writer(orderCreateWriterV4())
            .build();
    }

    @Bean
    public CustomQuerydslPagingItemReader<OrderDto> orderCreateReaderV4() {
        return new CustomQuerydslPagingItemReader<>(
            jpaQueryFactory(),
            chunkSize,
            queryFactory ->
                queryFactory
                    .select(new QOrderDto(
                        orderEntity.id,
                        orderEntity.amount,
                        orderEntity.status,
                        orderEntity.createdAt,
                        orderEntity.updatedAt))
                    .from(orderEntity)
                    .where(orderEntity.status.eq(OrderStatus.CREATED))
        );
    }

    @Bean
    public ItemProcessor<OrderDto, OrderDto> orderCreateProcessorV4() {
        return source -> {
            log.debug("신규 주문 DTO 처리 대상: {}", source.getId());
            return new OrderDto(
                source.getId(),
                source.getAmount(),
                OrderStatus.PROCESSING,
                source.getCreatedAt(),
                source.getUpdatedAt()
            );
        };
    }

    @Bean
    public ItemWriter<OrderDto> orderCreateWriterV4() {
        return new JdbcBatchItemWriterBuilder<OrderDto>()
            .dataSource(dataSource)
            .sql("""
                    UPDATE orders
                    SET status = :status, updated_at = :updatedAt
                    WHERE id = :id
                """)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .build();
    }
}
