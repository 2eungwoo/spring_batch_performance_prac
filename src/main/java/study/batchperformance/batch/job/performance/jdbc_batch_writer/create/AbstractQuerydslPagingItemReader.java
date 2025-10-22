package study.batchperformance.batch.job.performance.jdbc_batch_writer.create;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * QueryDSL 기반 ItemReader의 공통 로직을 추상화한 클래스
 * <p>
 * 각 페이징 전략(No-Offset, Offset)에 따라 doFetchPage() 메소드를 구현
 * 참고 블로그 : https://techblog.woowahan.com/2662/
 */
public abstract class AbstractQuerydslPagingItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> {

    protected final JPAQueryFactory queryFactory;
    protected final int pageSize;
    protected final Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    protected final Queue<T> results = new ConcurrentLinkedQueue<>();

    public AbstractQuerydslPagingItemReader(
            JPAQueryFactory queryFactory,
            int pageSize,
            Function<JPAQueryFactory, JPAQuery<T>> queryFunction
    ) {
        this.queryFactory = queryFactory;
        this.pageSize = pageSize;
        this.queryFunction = queryFunction;
        setName(ClassUtils.getShortName(getClass()));
    }

    @Override
    protected T doRead() throws Exception {
        if (results.isEmpty()) {
            doFetchPage();
        }
        return results.poll();
    }

    /**
     * 각 구현체에서 페이징 전략에 맞게 데이터를 조회하여 results 큐에 추가하는 메소드.
     */
    protected abstract void doFetchPage();

    @Override
    protected void doOpen() throws Exception {
        // 초기화 로직 (필요시)
    }

    @Override
    protected void doClose() throws Exception {
        // 리소스 해제 로직 (필요시)
        results.clear();
    }
}
