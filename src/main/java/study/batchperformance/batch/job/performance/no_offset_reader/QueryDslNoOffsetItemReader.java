package study.batchperformance.batch.job.performance.no_offset_reader;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.ItemReader;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class QueryDslNoOffsetItemReader<T> implements ItemReader<T> {

    protected final JPAQueryFactory queryFactory;
    private final int pageSize;
    private final NumberPath<Long> idPath;
    private final Function<T, Long> idExtractor;
    private final Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    private final Queue<T> results = new ConcurrentLinkedQueue<>();
    private long currentId = 0L;

    public QueryDslNoOffsetItemReader(
            JPAQueryFactory queryFactory,
            int pageSize,
            NumberPath<Long> idPath,
            Function<T, Long> idExtractor,
            Function<JPAQueryFactory, JPAQuery<T>> queryFunction
    ) {
        this.queryFactory = queryFactory;
        this.pageSize = pageSize;
        this.idPath = idPath;
        this.idExtractor = idExtractor;
        this.queryFunction = queryFunction;
    }

    @Override
    public T read() {
        if (results.isEmpty()) {
            fetch();
        }
        return results.poll();
    }

    protected void fetch() {
        // 기본 쿼리를 가져옴
        JPAQuery<T> query = queryFunction.apply(queryFactory);

        // No-Offset 조건 추가: id > currentId
        List<T> resultList = query
                .where(idPath.gt(currentId))
                .orderBy(idPath.asc())
                .limit(pageSize)
                .fetch();

        if (!resultList.isEmpty()) {
            results.addAll(resultList);
            // 마지막으로 조회된 ID를 업데이트
            currentId = idExtractor.apply(resultList.get(resultList.size() - 1));
        }
    }
}
