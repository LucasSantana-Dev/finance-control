package com.finance_control.unit.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.finance_control.shared.error.FrontendErrorLog;
import com.finance_control.shared.error.FrontendErrorLogRepository;
import com.finance_control.shared.error.FrontendErrorSeverity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class FrontendErrorLogRepositoryTest {

    @Autowired
    private FrontendErrorLogRepository repository;

    @Test
    void countBySeveritySince_ShouldReturnNumberOfRecentErrors() {
        Instant now = Instant.now();

        FrontendErrorLog recent = new FrontendErrorLog();
        recent.setMessage("Recent crash");
        recent.setSeverity(FrontendErrorSeverity.HIGH);
        recent.setOccurredAt(now.minus(1, ChronoUnit.MINUTES));
        recent.setReceivedAt(now.minus(1, ChronoUnit.MINUTES));
        repository.save(recent);

        FrontendErrorLog old = new FrontendErrorLog();
        old.setMessage("Old crash");
        old.setSeverity(FrontendErrorSeverity.HIGH);
        old.setOccurredAt(now.minus(3, ChronoUnit.HOURS));
        old.setReceivedAt(now.minus(3, ChronoUnit.HOURS));
        repository.save(old);

        long count = repository.countBySeveritySince(FrontendErrorSeverity.HIGH, now.minus(30, ChronoUnit.MINUTES));

        assertThat(count).isEqualTo(1);
    }
}
