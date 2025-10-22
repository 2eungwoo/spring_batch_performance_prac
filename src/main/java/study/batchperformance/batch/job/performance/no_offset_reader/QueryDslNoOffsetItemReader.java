package study.batchperformance.batch.job.performance.no_offset_reader;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.function.Function;
import study.batchperformance.batch.job.performance.jdbc_batch_writer.create.AbstractQuerydslPagingItemReader;

// 참고 블로그 : https://techblog.woowahan.com/2662/
public class QueryDslNoOffsetItemReader<T> extends AbstractQuerydslPagingItemReader<T> {

    private final NumberPath<Long> idPath;
    private final Function<T, Long> idExtractor;
    private long currentId = 0L;

    public QueryDslNoOffsetItemReader(
        JPAQueryFactory queryFactory,
        int pageSize,
        NumberPath<Long> idPath,
        Function<T, Long> idExtractor,
        Function<JPAQueryFactory, JPAQuery<T>> queryFunction
    ) {
        super(queryFactory, pageSize, queryFunction);
        this.idPath = idPath;
        this.idExtractor = idExtractor;
    }

    @Override
    protected void doFetchPage() {
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

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        currentId = 0L; // 재시작을 위해 currentId 초기화
    }
}