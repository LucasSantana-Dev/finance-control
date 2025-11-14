package com.finance_control.shared.error;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity that stores frontend-reported errors for auditing and analytics.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "frontend_error_log")
public class FrontendErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(name = "error_type", length = 255)
    private String errorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FrontendErrorSeverity severity;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "release", length = 128)
    private String release;

    @Column(name = "environment", length = 64)
    private String environment;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    @Column(name = "component", length = 255)
    private String component;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "browser", length = 255)
    private String browser;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Lob
    @Column(name = "stack_trace")
    private String stackTrace;

    @Lob
    @Column(name = "metadata_json")
    private String metadataJson;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
