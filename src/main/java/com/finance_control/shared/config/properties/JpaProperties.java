package com.finance_control.shared.config.properties;

/**
 * JPA configuration properties.
 */
public record JpaProperties(
    String hibernateDdlAuto,
    String dialect,
    boolean showSql,
    boolean formatSql,
    boolean useSqlComments,
    String namingStrategy,
    boolean deferDatasourceInitialization,
    HibernateProperties properties
) {
    public JpaProperties() {
        this("validate",
             "org.hibernate.dialect.PostgreSQLDialect",
             false,
             false,
             false,
             "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
             false,
             new HibernateProperties());
    }

    public record HibernateProperties(
        String hibernateFormatSql,
        String hibernateUseSqlComments,
        String hibernateJdbcBatchSize,
        String hibernateOrderInserts,
        String hibernateOrderUpdates,
        String hibernateBatchVersionedData,
        String hibernateJdbcFetchSize,
        String hibernateDefaultBatchFetchSize
    ) {
        public HibernateProperties() {
            this("false", "false", "20", "true", "true", "true", "20", "16");
        }
    }
}

