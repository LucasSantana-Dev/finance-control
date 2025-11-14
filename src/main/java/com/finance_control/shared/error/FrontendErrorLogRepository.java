package com.finance_control.shared.error;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FrontendErrorLogRepository extends JpaRepository<FrontendErrorLog, Long> {

    @Query("""
        SELECT COUNT(f)
        FROM FrontendErrorLog f
        WHERE f.severity = :severity AND f.receivedAt >= :since
    """)
    long countBySeveritySince(@Param("severity") FrontendErrorSeverity severity, @Param("since") Instant since);
}
