package study.batchperformance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class OrderJobTriggerController {

    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;

    @PostMapping("/order")
    public ResponseEntity<String> triggerOrderJob(
            @RequestParam(defaultValue = "orderJob") String jobName,
            @RequestParam(defaultValue = "manual") String triggerSource
    ) {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("triggerSource", triggerSource)
                .toJobParameters();
        try {
            jobLauncher.run(jobLocator.getJob(jobName), parameters);
            return ResponseEntity.ok(jobName + " triggered");
        } catch (NoSuchJobException ex) {
            log.warn("존재하지 않는 job 요청: {}", jobName, ex);
            return ResponseEntity.badRequest().body(jobName + " : 없는 jobName");
        } catch (Exception ex) {
            log.error("job 트리거 실패 {}", jobName, ex);
            return ResponseEntity.internalServerError().body(jobName + " 트리거 실패");
        }
    }
}
