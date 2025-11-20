package com.finance_control.unit.shared.util;

import com.finance_control.shared.exception.EntityMappingException;
import com.finance_control.shared.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityMapperTest {

    private SimpleSource source;
    private SimpleTarget target;

    @BeforeEach
    void setUp() {
        EntityMapper.clearCache();
        source = new SimpleSource();
        target = new SimpleTarget();
    }

    @Test
    void mapCommonFields_WithMatchingFields_ShouldMapFields() {
        source.setName("Test Name");
        source.setValue(100);
        source.setAmount(new BigDecimal("50.00"));

        EntityMapper.mapCommonFields(source, target);

        assertThat(target.getName()).isEqualTo("Test Name");
        assertThat(target.getValue()).isEqualTo(100);
        assertThat(target.getAmount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void mapCommonFields_WithNullSource_ShouldReturnTargetUnchanged() {
        target.setName("Original");
        SimpleTarget result = EntityMapper.mapCommonFields(null, target);

        assertThat(result).isSameAs(target);
        assertThat(result.getName()).isEqualTo("Original");
    }

    @Test
    void mapCommonFields_WithNullTarget_ShouldReturnNull() {
        source.setName("Test");
        SimpleTarget result = EntityMapper.mapCommonFields(source, null);

        assertThat(result).isNull();
    }

    @Test
    void mapCommonFields_WithNullValues_ShouldNotOverwriteExistingValues() {
        target.setName("Original");
        source.setName(null);
        source.setValue(100);

        EntityMapper.mapCommonFields(source, target);

        assertThat(target.getName()).isEqualTo("Original");
        assertThat(target.getValue()).isEqualTo(100);
    }

    @Test
    void mapCommonFields_WithIncompatibleTypes_ShouldSkipField() {
        source.setName("Test");
        source.setIncompatibleField("String");

        EntityMapper.mapCommonFields(source, target);

        assertThat(target.getName()).isEqualTo("Test");
        assertThat(target.getIncompatibleField()).isNull();
    }

    @Test
    void mapSpecificFields_WithValidFields_ShouldMapOnlySpecifiedFields() {
        source.setName("Test Name");
        source.setValue(100);

        EntityMapper.mapSpecificFields(source, target, "name");

        assertThat(target.getName()).isEqualTo("Test Name");
        assertThat(target.getValue()).isNull();
    }

    @Test
    void mapSpecificFields_WithNullSource_ShouldReturnTargetUnchanged() {
        target.setName("Original");
        SimpleTarget result = EntityMapper.mapSpecificFields(null, target, "name");

        assertThat(result).isSameAs(target);
        assertThat(result.getName()).isEqualTo("Original");
    }

    @Test
    void mapSpecificFields_WithNullTarget_ShouldReturnNull() {
        source.setName("Test");
        SimpleTarget result = EntityMapper.mapSpecificFields(source, null, "name");

        assertThat(result).isNull();
    }

    @Test
    void mapSpecificFields_WithNonExistentField_ShouldSkipField() {
        source.setName("Test");
        EntityMapper.mapSpecificFields(source, target, "name", "nonExistent");

        assertThat(target.getName()).isEqualTo("Test");
    }

    @Test
    void setFieldValue_WithValidField_ShouldSetValue() {
        EntityMapper.setFieldValue(target, "name", "New Name");

        assertThat(target.getName()).isEqualTo("New Name");
    }

    @Test
    void setFieldValue_WithNonExistentField_ShouldDoNothing() {
        target.setName("Original");
        EntityMapper.setFieldValue(target, "nonExistent", "value");

        assertThat(target.getName()).isEqualTo("Original");
    }

    @Test
    void getFieldValue_WithValidField_ShouldReturnValue() {
        target.setName("Test Name");
        target.setValue(100);

        Object name = EntityMapper.getFieldValue(target, "name");
        Object value = EntityMapper.getFieldValue(target, "value");

        assertThat(name).isEqualTo("Test Name");
        assertThat(value).isEqualTo(100);
    }

    @Test
    void getFieldValue_WithNonExistentField_ShouldReturnNull() {
        Object result = EntityMapper.getFieldValue(target, "nonExistent");

        assertThat(result).isNull();
    }

    @Test
    void getFieldValue_WithGetterException_ShouldThrowException() {
        SourceWithException source = new SourceWithException();

        assertThatThrownBy(() -> EntityMapper.getFieldValue(source, "problematicField"))
                .isInstanceOf(EntityMappingException.class)
                .hasMessageContaining("Failed to get field");
    }

    @Test
    void clearCache_ShouldClearMethodCache() {
        source.setName("Test");
        EntityMapper.mapCommonFields(source, target);

        EntityMapper.clearCache();

        SimpleTarget newTarget = new SimpleTarget();
        EntityMapper.mapCommonFields(source, newTarget);

        assertThat(newTarget.getName()).isEqualTo("Test");
    }

    @Test
    void mapCommonFields_WithBigDecimal_ShouldMapCorrectly() {
        source.setAmount(new BigDecimal("50.00"));
        EntityMapper.mapCommonFields(source, target);

        assertThat(target.getAmount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void mapCommonFields_WithCachedMethods_ShouldUseCache() {
        source.setName("Test");
        EntityMapper.mapCommonFields(source, target);

        SimpleTarget target2 = new SimpleTarget();
        EntityMapper.mapCommonFields(source, target2);

        assertThat(target2.getName()).isEqualTo("Test");
    }

    public static class SimpleSource {
        private String name;
        private Integer value;
        private BigDecimal amount;
        private String incompatibleField;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getIncompatibleField() {
            return incompatibleField;
        }

        public void setIncompatibleField(String incompatibleField) {
            this.incompatibleField = incompatibleField;
        }
    }

    public static class SimpleTarget {
        private String name;
        private Integer value;
        private BigDecimal amount;
        private Integer incompatibleField;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Integer getIncompatibleField() {
            return incompatibleField;
        }

        public void setIncompatibleField(Integer incompatibleField) {
            this.incompatibleField = incompatibleField;
        }
    }

    public static class SourceWithException {
        public String getProblematicField() {
            throw new RuntimeException("Getter exception");
        }
    }
}
