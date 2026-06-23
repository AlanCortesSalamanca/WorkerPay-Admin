package com.workerpay.debt.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.debt.service.DebtService;
import com.workerpay.worker.service.WorkerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DebtController.class)
class DebtControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DebtService debtService;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @WithMockUser(roles = "ADMIN")
    @Test
    void debtsListShouldReturnOk() throws Exception {
        when(debtService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/debts"))
            .andExpect(status().isOk());
    }
}
