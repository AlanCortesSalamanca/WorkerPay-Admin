SET client_encoding = 'UTF8';

BEGIN;

-- WorkerPay Admin demo data for local development.
-- This script is manual and idempotent enough to be run more than once.
-- It never deletes users, roles, or existing business data.
-- All demo text is ASCII to avoid Windows SQL Shell encoding issues.

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'allen', NULL, 'bebe', 'WEEKLY', 150.00, DATE '2026-01-01', TRUE, TIMESTAMP '2026-06-01 08:00:00', TIMESTAMP '2026-06-01 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'allen');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'alan', NULL, 'ing', 'WEEKLY', 150.00, DATE '2026-01-01', TRUE, TIMESTAMP '2026-06-01 08:05:00', TIMESTAMP '2026-06-01 08:05:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'alan');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'Carlos Mendoza Ruiz', NULL, 'Albanil', 'WEEKLY', 2800.00, DATE '2026-02-03', TRUE, TIMESTAMP '2026-06-01 08:10:00', TIMESTAMP '2026-06-01 08:10:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'Carlos Mendoza Ruiz');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'Maria Fernanda Lopez', NULL, 'Auxiliar Administrativo', 'BIWEEKLY', 6500.00, DATE '2026-02-10', TRUE, TIMESTAMP '2026-06-01 08:15:00', TIMESTAMP '2026-06-01 08:15:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'Maria Fernanda Lopez');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'Jose Luis Hernandez', NULL, 'Chofer', 'WEEKLY', 3200.00, DATE '2026-02-17', TRUE, TIMESTAMP '2026-06-01 08:20:00', TIMESTAMP '2026-06-01 08:20:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'Jose Luis Hernandez');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'Ana Gabriela Torres', NULL, 'Supervisora', 'BIWEEKLY', 8200.00, DATE '2026-02-24', TRUE, TIMESTAMP '2026-06-01 08:25:00', TIMESTAMP '2026-06-01 08:25:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'Ana Gabriela Torres');

INSERT INTO workers (full_name, phone, position, payment_type, base_salary, hire_date, active, created_at, updated_at)
SELECT 'Miguel Angel Ramirez', NULL, 'Ayudante General', 'WEEKLY', 2400.00, DATE '2026-03-03', TRUE, TIMESTAMP '2026-06-01 08:30:00', TIMESTAMP '2026-06-01 08:30:00'
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE full_name = 'Miguel Angel Ramirez');

INSERT INTO advances (worker_id, amount, date, reason, status, created_at, updated_at)
SELECT w.id, 500.00, DATE '2026-06-18', 'Adelanto semanal para gastos personales', 'PENDING', TIMESTAMP '2026-06-18 09:00:00', TIMESTAMP '2026-06-18 09:00:00'
FROM workers w
WHERE w.full_name = 'Carlos Mendoza Ruiz'
  AND NOT EXISTS (
      SELECT 1 FROM advances a
      WHERE a.worker_id = w.id AND a.amount = 500.00 AND a.date = DATE '2026-06-18' AND a.reason = 'Adelanto semanal para gastos personales'
  );

INSERT INTO advances (worker_id, amount, date, reason, status, created_at, updated_at)
SELECT w.id, 300.00, DATE '2026-06-12', 'Adelanto descontado en pago anterior', 'DISCOUNTED', TIMESTAMP '2026-06-12 09:30:00', TIMESTAMP '2026-06-16 17:00:00'
FROM workers w
WHERE w.full_name = 'Jose Luis Hernandez'
  AND NOT EXISTS (
      SELECT 1 FROM advances a
      WHERE a.worker_id = w.id AND a.amount = 300.00 AND a.date = DATE '2026-06-12' AND a.reason = 'Adelanto descontado en pago anterior'
  );

INSERT INTO advances (worker_id, amount, date, reason, status, created_at, updated_at)
SELECT w.id, 250.00, DATE '2026-06-19', 'Apoyo de transporte', 'PENDING', TIMESTAMP '2026-06-19 10:00:00', TIMESTAMP '2026-06-19 10:00:00'
FROM workers w
WHERE w.full_name = 'Miguel Angel Ramirez'
  AND NOT EXISTS (
      SELECT 1 FROM advances a
      WHERE a.worker_id = w.id AND a.amount = 250.00 AND a.date = DATE '2026-06-19' AND a.reason = 'Apoyo de transporte'
  );

INSERT INTO advances (worker_id, amount, date, reason, status, created_at, updated_at)
SELECT w.id, 700.00, DATE '2026-06-08', 'Solicitud cancelada', 'CANCELLED', TIMESTAMP '2026-06-08 11:00:00', TIMESTAMP '2026-06-09 12:00:00'
FROM workers w
WHERE w.full_name = 'Ana Gabriela Torres'
  AND NOT EXISTS (
      SELECT 1 FROM advances a
      WHERE a.worker_id = w.id AND a.amount = 700.00 AND a.date = DATE '2026-06-08' AND a.reason = 'Solicitud cancelada'
  );

INSERT INTO advances (worker_id, amount, date, reason, status, created_at, updated_at)
SELECT w.id, 400.00, DATE '2026-06-20', 'Adelanto administrativo', 'PENDING', TIMESTAMP '2026-06-20 09:15:00', TIMESTAMP '2026-06-20 09:15:00'
FROM workers w
WHERE w.full_name = 'Maria Fernanda Lopez'
  AND NOT EXISTS (
      SELECT 1 FROM advances a
      WHERE a.worker_id = w.id AND a.amount = 400.00 AND a.date = DATE '2026-06-20' AND a.reason = 'Adelanto administrativo'
  );

INSERT INTO debts (worker_id, original_amount, current_balance, suggested_payment, description, status, created_at, updated_at)
SELECT w.id, 3000.00, 2500.00, 500.00, 'Prestamo personal autorizado', 'ACTIVE', TIMESTAMP '2026-06-05 09:00:00', TIMESTAMP '2026-06-16 17:00:00'
FROM workers w
WHERE w.full_name = 'Carlos Mendoza Ruiz'
  AND NOT EXISTS (
      SELECT 1 FROM debts d
      WHERE d.worker_id = w.id AND d.original_amount = 3000.00 AND d.description = 'Prestamo personal autorizado'
  );

INSERT INTO debts (worker_id, original_amount, current_balance, suggested_payment, description, status, created_at, updated_at)
SELECT w.id, 2000.00, 0.00, 500.00, 'Deuda liquidada por reparacion de herramienta', 'PAID', TIMESTAMP '2026-06-03 09:00:00', TIMESTAMP '2026-06-14 17:00:00'
FROM workers w
WHERE w.full_name = 'Jose Luis Hernandez'
  AND NOT EXISTS (
      SELECT 1 FROM debts d
      WHERE d.worker_id = w.id AND d.original_amount = 2000.00 AND d.description = 'Deuda liquidada por reparacion de herramienta'
  );

INSERT INTO debts (worker_id, original_amount, current_balance, suggested_payment, description, status, created_at, updated_at)
SELECT w.id, 1500.00, 1500.00, 300.00, 'Apoyo economico pendiente', 'ACTIVE', TIMESTAMP '2026-06-10 10:00:00', TIMESTAMP '2026-06-10 10:00:00'
FROM workers w
WHERE w.full_name = 'Miguel Angel Ramirez'
  AND NOT EXISTS (
      SELECT 1 FROM debts d
      WHERE d.worker_id = w.id AND d.original_amount = 1500.00 AND d.description = 'Apoyo economico pendiente'
  );

INSERT INTO debts (worker_id, original_amount, current_balance, suggested_payment, description, status, created_at, updated_at)
SELECT w.id, 5000.00, 5000.00, 1000.00, 'Prestamo cancelado antes de aplicar', 'CANCELLED', TIMESTAMP '2026-06-06 11:00:00', TIMESTAMP '2026-06-07 12:00:00'
FROM workers w
WHERE w.full_name = 'Ana Gabriela Torres'
  AND NOT EXISTS (
      SELECT 1 FROM debts d
      WHERE d.worker_id = w.id AND d.original_amount = 5000.00 AND d.description = 'Prestamo cancelado antes de aplicar'
  );

INSERT INTO debts (worker_id, original_amount, current_balance, suggested_payment, description, status, created_at, updated_at)
SELECT w.id, 2500.00, 1800.00, 700.00, 'Prestamo administrativo', 'ACTIVE', TIMESTAMP '2026-06-04 09:30:00', TIMESTAMP '2026-06-15 17:00:00'
FROM workers w
WHERE w.full_name = 'Maria Fernanda Lopez'
  AND NOT EXISTS (
      SELECT 1 FROM debts d
      WHERE d.worker_id = w.id AND d.original_amount = 2500.00 AND d.description = 'Prestamo administrativo'
  );

INSERT INTO debt_payments (debt_id, amount, payment_date, notes, created_at)
SELECT d.id, 500.00, DATE '2026-06-16', 'Abono descontado en pago semana 24', TIMESTAMP '2026-06-16 17:05:00'
FROM debts d
JOIN workers w ON w.id = d.worker_id
WHERE w.full_name = 'Carlos Mendoza Ruiz'
  AND d.original_amount = 3000.00
  AND d.description = 'Prestamo personal autorizado'
  AND NOT EXISTS (
      SELECT 1 FROM debt_payments dp
      WHERE dp.debt_id = d.id AND dp.amount = 500.00 AND dp.payment_date = DATE '2026-06-16' AND dp.notes = 'Abono descontado en pago semana 24'
  );

INSERT INTO debt_payments (debt_id, amount, payment_date, notes, created_at)
SELECT d.id, 1000.00, DATE '2026-06-07', 'Primer abono de deuda liquidada', TIMESTAMP '2026-06-07 15:00:00'
FROM debts d
JOIN workers w ON w.id = d.worker_id
WHERE w.full_name = 'Jose Luis Hernandez'
  AND d.original_amount = 2000.00
  AND d.description = 'Deuda liquidada por reparacion de herramienta'
  AND NOT EXISTS (
      SELECT 1 FROM debt_payments dp
      WHERE dp.debt_id = d.id AND dp.amount = 1000.00 AND dp.payment_date = DATE '2026-06-07' AND dp.notes = 'Primer abono de deuda liquidada'
  );

INSERT INTO debt_payments (debt_id, amount, payment_date, notes, created_at)
SELECT d.id, 1000.00, DATE '2026-06-14', 'Segundo abono de deuda liquidada', TIMESTAMP '2026-06-14 15:00:00'
FROM debts d
JOIN workers w ON w.id = d.worker_id
WHERE w.full_name = 'Jose Luis Hernandez'
  AND d.original_amount = 2000.00
  AND d.description = 'Deuda liquidada por reparacion de herramienta'
  AND NOT EXISTS (
      SELECT 1 FROM debt_payments dp
      WHERE dp.debt_id = d.id AND dp.amount = 1000.00 AND dp.payment_date = DATE '2026-06-14' AND dp.notes = 'Segundo abono de deuda liquidada'
  );

INSERT INTO debt_payments (debt_id, amount, payment_date, notes, created_at)
SELECT d.id, 700.00, DATE '2026-06-15', 'Abono quincenal administrativo', TIMESTAMP '2026-06-15 17:05:00'
FROM debts d
JOIN workers w ON w.id = d.worker_id
WHERE w.full_name = 'Maria Fernanda Lopez'
  AND d.original_amount = 2500.00
  AND d.description = 'Prestamo administrativo'
  AND NOT EXISTS (
      SELECT 1 FROM debt_payments dp
      WHERE dp.debt_id = d.id AND dp.amount = 700.00 AND dp.payment_date = DATE '2026-06-15' AND dp.notes = 'Abono quincenal administrativo'
  );

INSERT INTO payment_periods (name, start_date, end_date, status, created_at, updated_at)
SELECT 'Semana 24 - Junio 2026', DATE '2026-06-10', DATE '2026-06-16', 'CLOSED', TIMESTAMP '2026-06-10 08:00:00', TIMESTAMP '2026-06-16 18:00:00'
WHERE NOT EXISTS (SELECT 1 FROM payment_periods WHERE name = 'Semana 24 - Junio 2026');

INSERT INTO payment_periods (name, start_date, end_date, status, created_at, updated_at)
SELECT 'Semana 25 - Junio 2026', DATE '2026-06-17', DATE '2026-06-23', 'OPEN', TIMESTAMP '2026-06-17 08:00:00', TIMESTAMP '2026-06-17 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM payment_periods WHERE name = 'Semana 25 - Junio 2026');

INSERT INTO payment_periods (name, start_date, end_date, status, created_at, updated_at)
SELECT 'Quincena 1 - Junio 2026', DATE '2026-06-01', DATE '2026-06-15', 'CLOSED', TIMESTAMP '2026-06-01 08:00:00', TIMESTAMP '2026-06-15 18:00:00'
WHERE NOT EXISTS (SELECT 1 FROM payment_periods WHERE name = 'Quincena 1 - Junio 2026');

INSERT INTO payment_periods (name, start_date, end_date, status, created_at, updated_at)
SELECT 'Quincena 2 - Junio 2026', DATE '2026-06-16', DATE '2026-06-30', 'OPEN', TIMESTAMP '2026-06-16 08:00:00', TIMESTAMP '2026-06-16 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM payment_periods WHERE name = 'Quincena 2 - Junio 2026');

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 2800.00, 200.00, 0.00, 500.00, 0.00, 2500.00, 'PAID', DATE '2026-06-16', TIMESTAMP '2026-06-16 17:10:00', TIMESTAMP '2026-06-16 17:10:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Semana 24 - Junio 2026'
WHERE w.full_name = 'Carlos Mendoza Ruiz'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 3200.00, 0.00, 300.00, 0.00, 100.00, 2800.00, 'PAID', DATE '2026-06-16', TIMESTAMP '2026-06-16 17:15:00', TIMESTAMP '2026-06-16 17:15:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Semana 24 - Junio 2026'
WHERE w.full_name = 'Jose Luis Hernandez'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 2400.00, 150.00, 0.00, 0.00, 0.00, 2550.00, 'PAID', DATE '2026-06-16', TIMESTAMP '2026-06-16 17:20:00', TIMESTAMP '2026-06-16 17:20:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Semana 24 - Junio 2026'
WHERE w.full_name = 'Miguel Angel Ramirez'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 2800.00, 0.00, 500.00, 500.00, 0.00, 1800.00, 'PENDING', DATE '2026-06-23', TIMESTAMP '2026-06-22 12:00:00', TIMESTAMP '2026-06-22 12:00:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Semana 25 - Junio 2026'
WHERE w.full_name = 'Carlos Mendoza Ruiz'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 2400.00, 100.00, 250.00, 300.00, 0.00, 1950.00, 'PENDING', DATE '2026-06-23', TIMESTAMP '2026-06-22 12:10:00', TIMESTAMP '2026-06-22 12:10:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Semana 25 - Junio 2026'
WHERE w.full_name = 'Miguel Angel Ramirez'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 6500.00, 500.00, 0.00, 700.00, 0.00, 6300.00, 'PAID', DATE '2026-06-15', TIMESTAMP '2026-06-15 17:10:00', TIMESTAMP '2026-06-15 17:10:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Quincena 1 - Junio 2026'
WHERE w.full_name = 'Maria Fernanda Lopez'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 8200.00, 800.00, 0.00, 0.00, 200.00, 8800.00, 'PAID', DATE '2026-06-15', TIMESTAMP '2026-06-15 17:15:00', TIMESTAMP '2026-06-15 17:15:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Quincena 1 - Junio 2026'
WHERE w.full_name = 'Ana Gabriela Torres'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 6500.00, 0.00, 400.00, 700.00, 0.00, 5400.00, 'PENDING', DATE '2026-06-30', TIMESTAMP '2026-06-22 12:20:00', TIMESTAMP '2026-06-22 12:20:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Quincena 2 - Junio 2026'
WHERE w.full_name = 'Maria Fernanda Lopez'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

INSERT INTO payroll_payments (worker_id, period_id, base_amount, bonuses, advance_discount, debt_discount, other_discounts, net_payment, status, payment_date, created_at, updated_at)
SELECT w.id, p.id, 8200.00, 0.00, 0.00, 0.00, 0.00, 8200.00, 'CANCELLED', DATE '2026-06-30', TIMESTAMP '2026-06-22 12:30:00', TIMESTAMP '2026-06-22 12:30:00'
FROM workers w
JOIN payment_periods p ON p.name = 'Quincena 2 - Junio 2026'
WHERE w.full_name = 'Ana Gabriela Torres'
  AND NOT EXISTS (SELECT 1 FROM payroll_payments pp WHERE pp.worker_id = w.id AND pp.period_id = p.id);

COMMIT;

SELECT COUNT(*) AS workers_count FROM workers;
SELECT COUNT(*) AS advances_count FROM advances;
SELECT COUNT(*) AS debts_count FROM debts;
SELECT COUNT(*) AS debt_payments_count FROM debt_payments;
SELECT COUNT(*) AS payment_periods_count FROM payment_periods;
SELECT COUNT(*) AS payroll_payments_count FROM payroll_payments;

SELECT COALESCE(SUM(amount), 0.00) AS pending_advances_total
FROM advances
WHERE status = 'PENDING';

SELECT COALESCE(SUM(current_balance), 0.00) AS active_debts_total
FROM debts
WHERE status = 'ACTIVE';

SELECT COUNT(*) AS pending_payments_count
FROM payroll_payments
WHERE status = 'PENDING';

SELECT COALESCE(SUM(net_payment), 0.00) AS paid_payments_total
FROM payroll_payments
WHERE status = 'PAID';
