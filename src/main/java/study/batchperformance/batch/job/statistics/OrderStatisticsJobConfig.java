package study.batchperformance.batch.job.statistics;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
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
                .tasklet(orderStatisticsTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet orderStatisticsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("주문 통계 집계 시작");

            // 1. DB의 group by 기능을 활용하여 모든 주문 통계를 한 번에 계산
            List<OrderStatisticsProjection> projections = orderJpaRepository.aggregateByStatus();

            // 2. 통계 엔티티로 변환
            List<OrderStatisticsEntity> statistics = projections.stream()
                    .map(p -> OrderStatisticsEntity.of(p.getStatus(), p.getOrderCount(), p.getTotalAmount()))
                    .collect(Collectors.toList());

            // 3. 기존 통계 데이터를 모두 삭제하여 멱등성 보장
            orderStatisticsRepository.deleteAllInBatch();

            // 4. 새로 계산된 통계 데이터를 한 번에 저장
            orderStatisticsRepository.saveAll(statistics);

            log.info("주문 통계 집계 완료. 총 {}건의 상태에 대한 통계 생성.", statistics.size());
            return RepeatStatus.FINISHED;
        };
    }
}