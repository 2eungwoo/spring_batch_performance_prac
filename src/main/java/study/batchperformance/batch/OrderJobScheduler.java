package study.batchperformance.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job orderJob;

    @Scheduled(cron = "*/10 * * * * *")
    public void launchOrderJob() {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(orderJob, parameters);
        } catch (Exception ex) {
            log.error("주문 배치 실행 중 오류가 발생했습니다.", ex);
        }
    }
}
