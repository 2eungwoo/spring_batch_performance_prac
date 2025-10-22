### Batch Processing Performance Improvements
> A Spring Boot project demonstrating and solving common performance bottlenecks in Spring Batch, particularly focusing on JPA-related issues in large-scale data processing.

### Project Overview
> This project explores various strategies to optimize batch processing performance, including:
> -   **JPA Batch Write Limitations**: Analysis of why JPA's default behavior (e.g., `IDENTITY` ID strategy, persistence context overhead) is inefficient for bulk `INSERT` and `UPDATE` operations in batch jobs.
> -   **Optimized Batch Writing**: Implementation of `JdbcBatchItemWriter` for high-performance batch `UPDATE`s, bypassing JPA's limitations.
> -   **Reader Performance**: Techniques to reduce `ItemReader` overhead by using DTO projections with QueryDSL and custom `QuerydslPagingItemReader` implementations, avoiding the JPA persistence context.
> -   **Architectural Considerations**: Examination of a statistics aggregation batch, highlighting the shift from RDB-heavy `GROUP BY` operations to a more distributed "Read-Process-Write" model.

### etc
> updated 2025-10-21
