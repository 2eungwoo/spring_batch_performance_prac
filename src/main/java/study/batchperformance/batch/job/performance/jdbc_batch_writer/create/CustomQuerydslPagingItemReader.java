package study.batchperformance.batch.job.performance.jdbc_batch_writer.create;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.function.Function;

// 참고 블로그 : https://techblog.woowahan.com/2662/
public class CustomQuerydslPagingItemReader<T> extends AbstractQuerydslPagingItemReader<T> {

    private int page = 0;

    public CustomQuerydslPagingItemReader(
            JPAQueryFactory queryFactory,
            int pageSize,
            Function<JPAQueryFactory, JPAQuery<T>> queryFunction
    ) {
        super(queryFactory, pageSize, queryFunction);
    }

    @Override
    protected void doFetchPage() {
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
    protected void doClose() throws Exception {
        super.doClose();
        page = 0; // 재시작을 위해 page 초기화
    }
}