SET client_encoding = 'UTF8';

BEGIN;

-- Run this once before deploying the hardened application with ddl-auto=validate.
-- It is idempotent for PostgreSQL and keeps existing data intact.

ALTER TABLE workers ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE advances ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE debts ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE payment_periods ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE payroll_payments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE debt_payments ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE debt_payments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE debt_payments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_workers_active ON workers (active);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_advances_worker_id ON advances (worker_id);
CREATE INDEX IF NOT EXISTS idx_advances_status ON advances (status);
CREATE INDEX IF NOT EXISTS idx_advances_worker_status ON advances (worker_id, status);
CREATE INDEX IF NOT EXISTS idx_debts_worker_id ON debts (worker_id);
CREATE INDEX IF NOT EXISTS idx_debts_status ON debts (status);
CREATE INDEX IF NOT EXISTS idx_debts_worker_status ON debts (worker_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_periods_status ON payment_periods (status);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_worker_id ON payroll_payments (worker_id);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_period_id ON payroll_payments (period_id);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_status ON payroll_payments (status);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_worker_period ON payroll_payments (worker_id, period_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_payroll_payments_active_worker_period ON payroll_payments (worker_id, period_id) WHERE status <> 'CANCELLED';
CREATE INDEX IF NOT EXISTS idx_debt_payments_debt_id ON debt_payments (debt_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles (role_id);

COMMIT;
