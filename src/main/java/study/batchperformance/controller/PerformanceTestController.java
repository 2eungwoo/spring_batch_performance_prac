package study.batchperformance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/batch/test")
@RequiredArgsConstructor
public class PerformanceTestController {

    private final JobLauncher jobLauncher;

    private final Job orderProcessRepositoryJob;
    private final Job orderCreateJob;
    private final Job orderProcessNoOffsetJob;

    /**
     * RepositoryItemReader
     */
    @PostMapping("/repository")
    public ResponseEntity<String> triggerRepositoryJob() throws Exception {
        return runJob(orderProcessRepositoryJob, "repositoryReaderTest");
    }

    /**
     * JpaPagingItemReader (offset)
     */
    @PostMapping("/jpa-paging")
    public ResponseEntity<String> triggerJpaPagingJob() throws Exception {
        return runJob(orderCreateJob, "jpaPagingReaderTest");
    }

    /**
     * QueryDslNoOffsetItemReader (no-offset)
     */
    @PostMapping("/no-offset")
    public ResponseEntity<String> triggerNoOffsetJob() throws Exception {
        return runJob(orderProcessNoOffsetJob, "noOffsetReaderTest");
    }

    private ResponseEntity<String> runJob(Job job, String triggerSource) throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("triggerSource", triggerSource)
                .toJobParameters();

        jobLauncher.run(job, parameters);
        String jobName = job.getName();
        log.info("job 실행: {}", jobName);
        return ResponseEntity.ok(jobName + " 실행");
    }
}
