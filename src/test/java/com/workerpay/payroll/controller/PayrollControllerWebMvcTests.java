package com.workerpay.payroll.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.payroll.service.PaymentPeriodService;
import com.workerpay.payroll.service.PayrollService;
import com.workerpay.worker.service.WorkerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PayrollController.class)
class PayrollControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollService payrollService;

    @MockBean
    private PaymentPeriodService paymentPeriodService;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @WithMockUser(roles = "ADMIN")
    @Test
    void payrollListShouldReturnOk() throws Exception {
        when(payrollService.findAllPayments()).thenReturn(List.of());

        mockMvc.perform(get("/payroll"))
            .andExpect(status().isOk());
    }
}
