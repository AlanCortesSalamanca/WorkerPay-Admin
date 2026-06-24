package com.workerpay.payroll.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.config.SecurityConfig;
import com.workerpay.payroll.service.PaymentPeriodService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentPeriodController.class)
@Import(SecurityConfig.class)
class PaymentPeriodControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentPeriodService paymentPeriodService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @WithMockUser(roles = "ADMIN")
    @Test
    void paymentPeriodsListShouldReturnOk() throws Exception {
        when(paymentPeriodService.findAllPeriods()).thenReturn(List.of());

        mockMvc.perform(get("/payment-periods"))
            .andExpect(status().isOk());
    }
}
