package study.batchperformance.batch.job.performance.jdbc_batch_writer.create;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * QueryDSL을 사용하여 페이징 방식으로 데이터를 읽어오는 커스텀 ItemReader.
 * Spring Batch의 QuerydslPagingItemReader가 Spring Boot 3 (Jakarta EE)와 호환되지 않아 직접 구현.
 * <p>
 * 이 Reader는 QueryDSL 쿼리를 사용하여 지정된 페이지 크기만큼 데이터를 조회합니다.
 * <p>
 * 주의: 이 Reader는 ItemStream이 아니므로, 재시작 시 상태를 저장/복원하지 않습니다.
 *      실제 운영 환경에서는 ItemStream을 구현하여 재시작 기능을 지원해야 합니다.
 */
public class CustomQuerydslPagingItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> {

    protected final JPAQueryFactory queryFactory;
    private final int pageSize;
    private final Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    private final Queue<T> results = new ConcurrentLinkedQueue<>();
    private int page = 0;

    public CustomQuerydslPagingItemReader(
            JPAQueryFactory queryFactory,
            int pageSize,
            Function<JPAQueryFactory, JPAQuery<T>> queryFunction
    ) {
        this.queryFactory = queryFactory;
        this.pageSize = pageSize;
        this.queryFunction = queryFunction;
        setName(ClassUtils.getShortName(CustomQuerydslPagingItemReader.class));
    }

    @Override
    protected T doRead() throws Exception {
        if (results.isEmpty()) {
            fetch();
        }
        return results.poll();
    }

    protected void fetch() {
        JPAQuery<T> query = queryFunction.apply(queryFactory);

        List<T> resultList = query
                .offset((long) page * pageSize)
                .limit(pageSize)
                .fetch();

        if (!resultList.isEmpty()) {
            results.addAll(resultList);
        }
        page++;
    }

    @Override
    protected void doOpen() throws Exception {
        // 초기화 로직 (필요시)
    }

    @Override
    protected void doClose() throws Exception {
        // 리소스 해제 로직 (필요시)
        results.clear();
        page = 0;
    }
}
