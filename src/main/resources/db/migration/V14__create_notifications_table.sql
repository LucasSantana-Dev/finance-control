-- Migration V14: Create notifications table
-- This table stores user notifications for various events (installments, goals, budgets, etc.)

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('INSTALLMENT_DUE', 'GOAL_PROGRESS', 'GOAL_ACHIEVED', 'BUDGET_ALERT')),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_id_created_at ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_id_is_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_user_type_read ON notifications(user_id, type, is_read);

-- Add comments for documentation
COMMENT ON TABLE notifications IS 'User notifications for various financial events';
COMMENT ON COLUMN notifications.type IS 'Notification type: INSTALLMENT_DUE, GOAL_PROGRESS, GOAL_ACHIEVED, BUDGET_ALERT';
COMMENT ON COLUMN notifications.metadata IS 'Additional notification data in JSON format';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when notification was marked as read';
