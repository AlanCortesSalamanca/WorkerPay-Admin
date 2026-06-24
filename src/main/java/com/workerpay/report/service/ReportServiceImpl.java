package com.workerpay.report.service;

import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.common.exception.ResourceNotFoundException;
import com.workerpay.common.util.CsvUtils;
import com.workerpay.common.util.MoneyUtils;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtPayment;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.repository.DebtPaymentRepository;
import com.workerpay.debt.repository.DebtRepository;
import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import com.workerpay.payroll.repository.PayrollPaymentRepository;
import com.workerpay.report.dto.ActiveDebtReportDTO;
import com.workerpay.report.dto.DebtPaymentHistoryDTO;
import com.workerpay.report.dto.FinancialSummaryDTO;
import com.workerpay.report.dto.PayrollByPeriodReportDTO;
import com.workerpay.report.dto.PendingAdvanceReportDTO;
import com.workerpay.report.dto.ReportFilterForm;
import com.workerpay.report.dto.ReportTotalsDTO;
import com.workerpay.report.dto.WorkerFinancialHistoryDTO;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.repository.WorkerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final PayrollPaymentRepository payrollPaymentRepository;
    private final AdvanceRepository advanceRepository;
    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final WorkerRepository workerRepository;

    public ReportServiceImpl(
        PayrollPaymentRepository payrollPaymentRepository,
        AdvanceRepository advanceRepository,
        DebtRepository debtRepository,
        DebtPaymentRepository debtPaymentRepository,
        WorkerRepository workerRepository
    ) {
        this.payrollPaymentRepository = payrollPaymentRepository;
        this.advanceRepository = advanceRepository;
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    public List<PayrollByPeriodReportDTO> getPayrollByPeriodReport(ReportFilterForm filter) {
        return filteredPayroll(filter).stream()
            .map(this::toPayrollReport)
            .toList();
    }

    @Override
    public ReportTotalsDTO getPayrollByPeriodTotals(ReportFilterForm filter) {
        List<PayrollPayment> payments = filteredPayroll(filter);
        return new ReportTotalsDTO(
            payments.size(),
            sum(payments.stream().map(PayrollPayment::getBaseAmount).toList()),
            sum(payments.stream().map(PayrollPayment::getBonuses).toList()),
            sum(payments.stream().map(PayrollPayment::getAdvanceDiscount).toList()),
            sum(payments.stream().map(PayrollPayment::getDebtDiscount).toList()),
            sum(payments.stream().map(PayrollPayment::getOtherDiscounts).toList()),
            sum(payments.stream().map(PayrollPayment::getNetPayment).toList()),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2)
        );
    }

    @Override
    public List<PendingAdvanceReportDTO> getPendingAdvancesReport(ReportFilterForm filter) {
        return filteredPendingAdvances(filter).stream()
            .map(this::toPendingAdvanceReport)
            .toList();
    }

    @Override
    public ReportTotalsDTO getPendingAdvancesTotals(ReportFilterForm filter) {
        List<Advance> advances = filteredPendingAdvances(filter);
        return emptyTotals(advances.size(), sum(advances.stream().map(Advance::getAmount).toList()), BigDecimal.ZERO);
    }

    @Override
    public List<ActiveDebtReportDTO> getActiveDebtsReport(ReportFilterForm filter) {
        return filteredActiveDebts(filter).stream()
            .map(this::toActiveDebtReport)
            .toList();
    }

    @Override
    public ReportTotalsDTO getActiveDebtsTotals(ReportFilterForm filter) {
        List<Debt> debts = filteredActiveDebts(filter);
        return emptyTotals(debts.size(), BigDecimal.ZERO, sum(debts.stream().map(Debt::getCurrentBalance).toList()));
    }

    @Override
    public WorkerFinancialHistoryDTO getWorkerFinancialHistory(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
            .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));
        List<PayrollPayment> payments = payrollPaymentRepository.findWithRelationsByWorkerId(workerId);
        List<Advance> advances = advanceRepository.findByWorkerId(workerId);
        List<Debt> debts = debtRepository.findByWorkerId(workerId);
        List<DebtPayment> debtPayments = debtPaymentRepository.findByDebtWorkerId(workerId);
        BigDecimal totalPaid = sum(payments.stream()
            .filter(payment -> payment.getStatus() == PayrollPaymentStatus.PAID)
            .map(PayrollPayment::getNetPayment)
            .toList());
        BigDecimal totalPendingAdvances = sum(advances.stream()
            .filter(advance -> advance.getStatus() == AdvanceStatus.PENDING)
            .map(Advance::getAmount)
            .toList());
        BigDecimal totalDiscountedAdvances = sum(advances.stream()
            .filter(advance -> advance.getStatus() == AdvanceStatus.DISCOUNTED)
            .map(Advance::getAmount)
            .toList());
        List<Debt> activeDebts = debts.stream()
            .filter(debt -> debt.getStatus() == DebtStatus.ACTIVE)
            .toList();
        return new WorkerFinancialHistoryDTO(
            worker.getId(),
            worker.getFullName(),
            worker.getPosition(),
            worker.getPaymentType().name(),
            worker.getBaseSalary(),
            worker.isActive(),
            payments.stream().sorted(byPaymentDateDesc()).map(this::toPayrollReport).toList(),
            advances.stream().sorted(byAdvanceDateDesc()).map(this::toAdvanceReport).toList(),
            debts.stream().sorted(byDebtCreatedAtDesc()).map(this::toActiveDebtReport).toList(),
            debtPayments.stream().sorted(byDebtPaymentDateDesc()).map(this::toDebtPaymentHistory).toList(),
            totalPaid,
            totalPendingAdvances,
            totalDiscountedAdvances,
            activeDebts.size(),
            sum(activeDebts.stream().map(Debt::getCurrentBalance).toList()),
            sum(debtPayments.stream().map(DebtPayment::getAmount).toList())
        );
    }

    @Override
    public FinancialSummaryDTO getFinancialSummary() {
        List<Advance> pendingAdvances = advanceRepository.findByStatus(AdvanceStatus.PENDING);
        List<Debt> activeDebts = debtRepository.findByStatus(DebtStatus.ACTIVE);
        List<PayrollPayment> paidPayments = payrollPaymentRepository.findWithRelationsByStatus(PayrollPaymentStatus.PAID);
        BigDecimal totalDiscounts = sum(paidPayments.stream().map(PayrollPayment::getAdvanceDiscount).toList())
            .add(sum(paidPayments.stream().map(PayrollPayment::getDebtDiscount).toList()))
            .add(sum(paidPayments.stream().map(PayrollPayment::getOtherDiscounts).toList()));
        return new FinancialSummaryDTO(
            workerRepository.countByActiveTrue(),
            pendingAdvances.size(),
            sum(pendingAdvances.stream().map(Advance::getAmount).toList()),
            activeDebts.size(),
            sum(activeDebts.stream().map(Debt::getCurrentBalance).toList()),
            payrollPaymentRepository.countByStatus(PayrollPaymentStatus.PENDING),
            sumGrossPayments(paidPayments),
            sum(paidPayments.stream().map(PayrollPayment::getNetPayment).toList()),
            MoneyUtils.normalize(totalDiscounts),
            payrollPaymentRepository.findTop5ByOrderByPaymentDateDesc().stream().map(this::toPayrollReport).toList(),
            advanceRepository.findTop5ByStatusOrderByDateDesc(AdvanceStatus.PENDING).stream().map(this::toPendingAdvanceReport).toList(),
            debtRepository.findTop5ByStatusOrderByCreatedAtDesc(DebtStatus.ACTIVE).stream().map(this::toActiveDebtReport).toList()
        );
    }

    @Override
    public String exportPayrollByPeriodCsv(ReportFilterForm filter) {
        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtils.row("Periodo", "Trabajador", "Sueldo base", "Bonos", "Adelantos", "Deuda", "Otros", "Neto", "Estado", "Fecha"));
        getPayrollByPeriodReport(filter).forEach(row -> csv.append(CsvUtils.row(
            row.periodName(), row.workerName(), row.baseAmount(), row.bonuses(), row.advanceDiscount(),
            row.debtDiscount(), row.otherDiscounts(), row.netPayment(), row.status(), row.paymentDate()
        )));
        return csv.toString();
    }

    @Override
    public String exportPendingAdvancesCsv(ReportFilterForm filter) {
        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtils.row("Trabajador", "Puesto", "Monto", "Fecha", "Motivo", "Estado", "Creacion"));
        getPendingAdvancesReport(filter).forEach(row -> csv.append(CsvUtils.row(
            row.workerName(), row.workerPosition(), row.amount(), row.date(), row.reason(), row.status(), row.createdAt()
        )));
        return csv.toString();
    }

    @Override
    public String exportActiveDebtsCsv(ReportFilterForm filter) {
        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtils.row("Trabajador", "Puesto", "Monto original", "Saldo actual", "Abono sugerido", "Descripcion", "Estado", "Creacion"));
        getActiveDebtsReport(filter).forEach(row -> csv.append(CsvUtils.row(
            row.workerName(), row.workerPosition(), row.originalAmount(), row.currentBalance(), row.suggestedPayment(),
            row.description(), row.status(), row.createdAt()
        )));
        return csv.toString();
    }

    @Override
    public String exportWorkerHistoryCsv(Long workerId) {
        WorkerFinancialHistoryDTO history = getWorkerFinancialHistory(workerId);
        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtils.row("Historial financiero", history.workerName()));
        csv.append(CsvUtils.row("Puesto", history.workerPosition(), "Tipo de pago", history.paymentType(), "Sueldo base", history.baseSalary()));
        csv.append(CsvUtils.row("Total pagado", history.totalPaid(), "Adelantos pendientes", history.totalPendingAdvances(), "Saldo deuda", history.totalPendingDebtBalance()));
        csv.append(CsvUtils.row());
        csv.append(CsvUtils.row("Pagos"));
        csv.append(CsvUtils.row("Periodo", "Trabajador", "Sueldo base", "Bonos", "Adelantos", "Deuda", "Otros", "Neto", "Estado", "Fecha"));
        history.payments().forEach(row -> csv.append(CsvUtils.row(
            row.periodName(), row.workerName(), row.baseAmount(), row.bonuses(), row.advanceDiscount(),
            row.debtDiscount(), row.otherDiscounts(), row.netPayment(), row.status(), row.paymentDate()
        )));
        csv.append(CsvUtils.row("Adelantos"));
        csv.append(CsvUtils.row("Trabajador", "Puesto", "Monto", "Fecha", "Motivo", "Estado", "Creacion"));
        history.advances().forEach(row -> csv.append(CsvUtils.row(
            row.workerName(), row.workerPosition(), row.amount(), row.date(), row.reason(), row.status(), row.createdAt()
        )));
        csv.append(CsvUtils.row("Deudas"));
        csv.append(CsvUtils.row("Trabajador", "Puesto", "Monto original", "Saldo actual", "Abono sugerido", "Descripcion", "Estado", "Creacion"));
        history.debts().forEach(row -> csv.append(CsvUtils.row(
            row.workerName(), row.workerPosition(), row.originalAmount(), row.currentBalance(), row.suggestedPayment(),
            row.description(), row.status(), row.createdAt()
        )));
        csv.append(CsvUtils.row("Abonos"));
        csv.append(CsvUtils.row("Trabajador", "Deuda", "Monto", "Fecha", "Notas", "Creacion"));
        history.debtPayments().forEach(row -> csv.append(CsvUtils.row(
            row.workerName(), row.debtDescription(), row.amount(), row.paymentDate(), row.notes(), row.createdAt()
        )));
        return csv.toString();
    }

    @Override
    public String exportFinancialSummaryCsv() {
        FinancialSummaryDTO summary = getFinancialSummary();
        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtils.row("Metrica", "Valor"));
        csv.append(CsvUtils.row("Trabajadores activos", summary.activeWorkersCount()));
        csv.append(CsvUtils.row("Adelantos pendientes", summary.pendingAdvancesCount()));
        csv.append(CsvUtils.row("Monto adelantos pendientes", summary.pendingAdvancesTotal()));
        csv.append(CsvUtils.row("Deudas activas", summary.activeDebtsCount()));
        csv.append(CsvUtils.row("Saldo deudas activas", summary.activeDebtsTotal()));
        csv.append(CsvUtils.row("Pagos pendientes", summary.pendingPaymentsCount()));
        csv.append(CsvUtils.row("Total pagado", summary.paidPaymentsTotal()));
        csv.append(CsvUtils.row("Total neto pagado", summary.netPaidTotal()));
        csv.append(CsvUtils.row("Total descuentos aplicados", summary.totalDiscounts()));
        return csv.toString();
    }

    private List<PayrollPayment> filteredPayroll(ReportFilterForm filter) {
        ReportFilterForm safeFilter = safe(filter);
        return payrollPaymentRepository.findFilteredForReport(
            safeFilter.getPeriodId(),
            payrollStatus(safeFilter.getStatus()),
            safeFilter.getStartDate(),
            safeFilter.getEndDate()
        );
    }

    private List<Advance> filteredPendingAdvances(ReportFilterForm filter) {
        ReportFilterForm safeFilter = safe(filter);
        return advanceRepository.findFilteredForReport(
            AdvanceStatus.PENDING,
            safeFilter.getWorkerId(),
            safeFilter.getStartDate(),
            safeFilter.getEndDate()
        );
    }

    private List<Debt> filteredActiveDebts(ReportFilterForm filter) {
        ReportFilterForm safeFilter = safe(filter);
        return debtRepository.findFilteredForReport(
            DebtStatus.ACTIVE,
            safeFilter.getWorkerId(),
            safeFilter.getMinAmount(),
            safeFilter.getMaxAmount()
        );
    }

    private PayrollByPeriodReportDTO toPayrollReport(PayrollPayment payment) {
        return new PayrollByPeriodReportDTO(
            payment.getPeriod().getName(),
            payment.getWorker().getFullName(),
            payment.getBaseAmount(),
            payment.getBonuses(),
            payment.getAdvanceDiscount(),
            payment.getDebtDiscount(),
            payment.getOtherDiscounts(),
            payment.getNetPayment(),
            payment.getStatus().name(),
            payment.getPaymentDate()
        );
    }

    private PendingAdvanceReportDTO toPendingAdvanceReport(Advance advance) {
        return toAdvanceReport(advance);
    }

    private PendingAdvanceReportDTO toAdvanceReport(Advance advance) {
        return new PendingAdvanceReportDTO(
            advance.getWorker().getFullName(),
            advance.getWorker().getPosition(),
            advance.getAmount(),
            advance.getDate(),
            advance.getReason(),
            advance.getStatus().name(),
            advance.getCreatedAt()
        );
    }

    private ActiveDebtReportDTO toActiveDebtReport(Debt debt) {
        return new ActiveDebtReportDTO(
            debt.getWorker().getFullName(),
            debt.getWorker().getPosition(),
            debt.getOriginalAmount(),
            debt.getCurrentBalance(),
            debt.getSuggestedPayment(),
            debt.getDescription(),
            debt.getStatus().name(),
            debt.getCreatedAt()
        );
    }

    private DebtPaymentHistoryDTO toDebtPaymentHistory(DebtPayment payment) {
        return new DebtPaymentHistoryDTO(
            payment.getDebt().getWorker().getFullName(),
            payment.getDebt().getDescription(),
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getNotes(),
            payment.getCreatedAt()
        );
    }

    private ReportTotalsDTO emptyTotals(long count, BigDecimal totalAmount, BigDecimal totalBalance) {
        return new ReportTotalsDTO(
            count,
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            MoneyUtils.normalize(totalAmount),
            MoneyUtils.normalize(totalBalance)
        );
    }

    private BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream()
            .map(MoneyUtils::normalize)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private BigDecimal sumGrossPayments(List<PayrollPayment> payments) {
        return payments.stream()
            .map(payment -> MoneyUtils.normalize(payment.getBaseAmount()).add(MoneyUtils.normalize(payment.getBonuses())))
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private PayrollPaymentStatus payrollStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PayrollPaymentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Estado de pago invalido.");
        }
    }

    private ReportFilterForm safe(ReportFilterForm filter) {
        return filter == null ? new ReportFilterForm() : filter;
    }

    private Comparator<PayrollPayment> byPaymentDateDesc() {
        return Comparator.comparing(PayrollPayment::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private Comparator<Advance> byAdvanceDateDesc() {
        return Comparator.comparing(Advance::getDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private Comparator<Debt> byDebtCreatedAtDesc() {
        return Comparator.comparing(Debt::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private Comparator<DebtPayment> byDebtPaymentDateDesc() {
        return Comparator.comparing(DebtPayment::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }
}
