package study.batchperformance.batch;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import study.batchperformance.domain.order.OrderEntity;
import study.batchperformance.domain.order.OrderStatus;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TutorialJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job batchJob() { // job 이름 변경
        return new JobBuilder("batch-job", jobRepository)
                .start(tutorialStep())
                .build();
    }

    @Bean
    public Step tutorialStep() {
        return new StepBuilder("tutorialStep", jobRepository)
                .<OrderEntity, OrderEntity>chunk(10, transactionManager)
                .reader(tutorialReader())
                .processor(tutorialProcessor())
                .writer(tutorialWriter())
                .build();
    }

    @Bean
    public ListItemReader<OrderEntity> tutorialReader() {
        List<OrderEntity> items = Arrays.asList(
                OrderEntity.builder().amount(BigDecimal.valueOf(1000)).status(OrderStatus.CREATED).build(),
                OrderEntity.builder().amount(BigDecimal.valueOf(2000)).status(OrderStatus.CREATED).build(),
                OrderEntity.builder().amount(BigDecimal.valueOf(3000)).status(OrderStatus.CREATED).build()
        );
        return new ListItemReader<>(items);
    }

    @Bean
    public ItemProcessor<OrderEntity, OrderEntity> tutorialProcessor() {
        return item -> {
            log.info("데이터 처리중, id: {}", item.getId());
            // item.setStatus(OrderStatus.PROCESSING); // OrderEntity에 Setter가 없으므로 주석 처리
            return item;
        };
    }

    @Bean
    public ItemWriter<OrderEntity> tutorialWriter() {
        return items -> {
            log.info("총 {}건의 데이터 쓰기 시작", items.size());
            for (OrderEntity item : items) {
                log.info("쓰기 완료된 데이터: {}", item);
            }
        };
    }
}
