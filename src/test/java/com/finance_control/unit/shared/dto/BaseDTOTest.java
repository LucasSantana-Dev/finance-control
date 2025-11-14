package com.finance_control.unit.shared.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.unit.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("BaseDTO Validation Tests")
class BaseDTOTest extends BaseUnitTest {

    private static class TestDTO extends BaseDTO<Long> {
        // Concrete implementation for testing
    }

    @Test
    @DisplayName("validateCreate - should not throw by default")
    void validateCreate_ShouldNotThrowByDefault() {
        TestDTO dto = new TestDTO();

        assertThatCode(() -> dto.validateCreate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUpdate - should not throw by default")
    void validateUpdate_ShouldNotThrowByDefault() {
        TestDTO dto = new TestDTO();

        assertThatCode(() -> dto.validateUpdate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with null id should throw")
    void validateResponse_WithNullId_ShouldThrow() {
        TestDTO dto = new TestDTO();
        dto.setId(null);

        assertThatThrownBy(() -> dto.validateResponse())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID is required");
    }

    @Test
    @DisplayName("validateResponse - with valid id should not throw")
    void validateResponse_WithValidId_ShouldNotThrow() {
        TestDTO dto = new TestDTO();
        dto.setId(1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with zero id should not throw")
    void validateResponse_WithZeroId_ShouldNotThrow() {
        TestDTO dto = new TestDTO();
        dto.setId(0L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateResponse - with negative id should not throw")
    void validateResponse_WithNegativeId_ShouldNotThrow() {
        TestDTO dto = new TestDTO();
        dto.setId(-1L);

        assertThatCode(() -> dto.validateResponse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Getters and setters should work correctly")
    void gettersAndSetters_ShouldWorkCorrectly() {
        TestDTO dto = new TestDTO();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(1L);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        assertThatCode(() -> {
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
        }).doesNotThrowAnyException();
    }
}
