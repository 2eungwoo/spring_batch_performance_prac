package study.batchperformance.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderJobScheduler {

    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;

    @Scheduled(cron = "*/10 * * * * *")
    public void launchOrderJob() {
        try {
            var job = jobLocator.getJob("orderJob");
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, parameters);
        } catch (NoSuchJobException ex) {
            log.warn("orderJob 이 등록되어 있지 않아 실행 실패", ex);
        } catch (Exception ex) {
            log.error("배치 실행 중 오류 발생.", ex);
        }
    }
}
