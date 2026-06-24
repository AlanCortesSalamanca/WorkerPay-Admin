package com.workerpay.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.workerpay.advance.entity.Advance;
import com.workerpay.advance.entity.AdvanceStatus;
import com.workerpay.advance.repository.AdvanceRepository;
import com.workerpay.debt.entity.Debt;
import com.workerpay.debt.entity.DebtPayment;
import com.workerpay.debt.entity.DebtStatus;
import com.workerpay.debt.repository.DebtPaymentRepository;
import com.workerpay.debt.repository.DebtRepository;
import com.workerpay.payroll.entity.PaymentPeriod;
import com.workerpay.payroll.entity.PaymentPeriodStatus;
import com.workerpay.payroll.entity.PayrollPayment;
import com.workerpay.payroll.entity.PayrollPaymentStatus;
import com.workerpay.payroll.repository.PayrollPaymentRepository;
import com.workerpay.report.dto.FinancialSummaryDTO;
import com.workerpay.report.dto.ReportFilterForm;
import com.workerpay.report.dto.WorkerFinancialHistoryDTO;
import com.workerpay.worker.entity.PaymentType;
import com.workerpay.worker.entity.Worker;
import com.workerpay.worker.repository.WorkerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTests {

    @Mock
    private PayrollPaymentRepository payrollPaymentRepository;

    @Mock
    private AdvanceRepository advanceRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtPaymentRepository debtPaymentRepository;

    @Mock
    private WorkerRepository workerRepository;

    private ReportServiceImpl reportService;
    private Worker worker;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
            payrollPaymentRepository,
            advanceRepository,
            debtRepository,
            debtPaymentRepository,
            workerRepository
        );
        worker = worker(1L, "Ana, \"Lopez\"");
    }

    @Test
    void financialSummaryCalculatesTotals() {
        PayrollPayment paid = payment(PayrollPaymentStatus.PAID);
        PayrollPayment pending = payment(PayrollPaymentStatus.PENDING);
        when(workerRepository.countByActiveTrue()).thenReturn(2L);
        when(advanceRepository.findByStatus(AdvanceStatus.PENDING)).thenReturn(List.of(advance(AdvanceStatus.PENDING, "100.00")));
        when(debtRepository.findByStatus(DebtStatus.ACTIVE)).thenReturn(List.of(debt(DebtStatus.ACTIVE, "300.00")));
        when(payrollPaymentRepository.findWithRelationsByStatus(PayrollPaymentStatus.PAID)).thenReturn(List.of(paid));
        when(payrollPaymentRepository.countByStatus(PayrollPaymentStatus.PENDING)).thenReturn(1L);
        when(payrollPaymentRepository.findTop5ByOrderByPaymentDateDesc()).thenReturn(List.of(paid, pending));
        when(advanceRepository.findTop5ByStatusOrderByDateDesc(AdvanceStatus.PENDING)).thenReturn(List.of());
        when(debtRepository.findTop5ByStatusOrderByCreatedAtDesc(DebtStatus.ACTIVE)).thenReturn(List.of());

        FinancialSummaryDTO summary = reportService.getFinancialSummary();

        assertThat(summary.activeWorkersCount()).isEqualTo(2L);
        assertThat(summary.pendingAdvancesTotal()).isEqualByComparingTo("100.00");
        assertThat(summary.activeDebtsTotal()).isEqualByComparingTo("300.00");
        assertThat(summary.pendingPaymentsCount()).isEqualTo(1L);
        assertThat(summary.paidPaymentsTotal()).isEqualByComparingTo("1025.00");
        assertThat(summary.netPaidTotal()).isEqualByComparingTo("850.00");
        assertThat(summary.totalDiscounts()).isEqualByComparingTo("175.00");
    }

    @Test
    void pendingAdvancesReportOnlyUsesPendingRepositoryResults() {
        when(advanceRepository.findFilteredForReport(AdvanceStatus.PENDING, null, null, null))
            .thenReturn(List.of(advance(AdvanceStatus.PENDING, "100.00")));

        assertThat(reportService.getPendingAdvancesReport(new ReportFilterForm()))
            .hasSize(1)
            .first()
            .extracting("status")
            .isEqualTo("PENDING");
    }

    @Test
    void activeDebtsReportOnlyUsesActiveRepositoryResults() {
        when(debtRepository.findFilteredForReport(DebtStatus.ACTIVE, null, null, null))
            .thenReturn(List.of(debt(DebtStatus.ACTIVE, "300.00")));

        assertThat(reportService.getActiveDebtsReport(new ReportFilterForm()))
            .hasSize(1)
            .first()
            .extracting("status")
            .isEqualTo("ACTIVE");
    }

    @Test
    void workerHistoryGroupsFinancialData() {
        Debt debt = debt(DebtStatus.ACTIVE, "300.00");
        DebtPayment debtPayment = debtPayment(debt);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(payrollPaymentRepository.findWithRelationsByWorkerId(1L)).thenReturn(List.of(payment(PayrollPaymentStatus.PAID)));
        when(advanceRepository.findByWorkerId(1L)).thenReturn(List.of(advance(AdvanceStatus.PENDING, "100.00")));
        when(debtRepository.findByWorkerId(1L)).thenReturn(List.of(debt));
        when(debtPaymentRepository.findByDebtWorkerId(1L)).thenReturn(List.of(debtPayment));

        WorkerFinancialHistoryDTO history = reportService.getWorkerFinancialHistory(1L);

        assertThat(history.payments()).hasSize(1);
        assertThat(history.advances()).hasSize(1);
        assertThat(history.debts()).hasSize(1);
        assertThat(history.debtPayments()).hasSize(1);
        assertThat(history.totalPaid()).isEqualByComparingTo("850.00");
        assertThat(history.totalPendingDebtBalance()).isEqualByComparingTo("300.00");
        assertThat(history.totalDebtPayments()).isEqualByComparingTo("50.00");
    }

    @Test
    void csvEscapesCommasAndQuotes() {
        Advance advance = advance(AdvanceStatus.PENDING, "100.00");
        advance.setReason("Apoyo, \"especial\"");
        when(advanceRepository.findFilteredForReport(AdvanceStatus.PENDING, null, null, null)).thenReturn(List.of(advance));

        String csv = reportService.exportPendingAdvancesCsv(new ReportFilterForm());

        assertThat(csv).contains("\"Ana, \"\"Lopez\"\"\"");
        assertThat(csv).contains("\"Apoyo, \"\"especial\"\"\"");
    }

    private PayrollPayment payment(PayrollPaymentStatus status) {
        PayrollPayment payment = new PayrollPayment();
        payment.setWorker(worker);
        payment.setPeriod(period());
        payment.setBaseAmount(new BigDecimal("1000.00"));
        payment.setBonuses(new BigDecimal("25.00"));
        payment.setAdvanceDiscount(new BigDecimal("100.00"));
        payment.setDebtDiscount(new BigDecimal("50.00"));
        payment.setOtherDiscounts(new BigDecimal("25.00"));
        payment.setNetPayment(new BigDecimal("850.00"));
        payment.setStatus(status);
        payment.setPaymentDate(LocalDate.of(2026, 6, 22));
        return payment;
    }

    private Advance advance(AdvanceStatus status, String amount) {
        Advance advance = new Advance();
        advance.setWorker(worker);
        advance.setAmount(new BigDecimal(amount));
        advance.setDate(LocalDate.of(2026, 6, 20));
        advance.setReason("Apoyo");
        advance.setStatus(status);
        ReflectionTestUtils.setField(advance, "createdAt", LocalDateTime.of(2026, 6, 20, 10, 0));
        ReflectionTestUtils.setField(advance, "updatedAt", LocalDateTime.of(2026, 6, 20, 10, 0));
        return advance;
    }

    private Debt debt(DebtStatus status, String balance) {
        Debt debt = new Debt();
        debt.setWorker(worker);
        debt.setOriginalAmount(new BigDecimal("400.00"));
        debt.setCurrentBalance(new BigDecimal(balance));
        debt.setSuggestedPayment(new BigDecimal("50.00"));
        debt.setDescription("Prestamo");
        debt.setStatus(status);
        ReflectionTestUtils.setField(debt, "createdAt", LocalDateTime.of(2026, 6, 19, 10, 0));
        ReflectionTestUtils.setField(debt, "updatedAt", LocalDateTime.of(2026, 6, 19, 10, 0));
        return debt;
    }

    private DebtPayment debtPayment(Debt debt) {
        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setPaymentDate(LocalDate.of(2026, 6, 21));
        payment.setNotes("Abono");
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.of(2026, 6, 21, 10, 0));
        return payment;
    }

    private PaymentPeriod period() {
        PaymentPeriod period = new PaymentPeriod();
        ReflectionTestUtils.setField(period, "id", 2L);
        period.setName("Junio 2026");
        period.setStartDate(LocalDate.of(2026, 6, 1));
        period.setEndDate(LocalDate.of(2026, 6, 15));
        period.setStatus(PaymentPeriodStatus.OPEN);
        return period;
    }

    private Worker worker(Long id, String name) {
        Worker worker = new Worker();
        ReflectionTestUtils.setField(worker, "id", id);
        worker.setFullName(name);
        worker.setPosition("Produccion");
        worker.setPaymentType(PaymentType.WEEKLY);
        worker.setBaseSalary(new BigDecimal("1000.00"));
        worker.setHireDate(LocalDate.of(2026, 1, 1));
        worker.setActive(true);
        return worker;
    }
}
