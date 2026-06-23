package com.workerpay.advance.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.workerpay.advance.service.AdvanceService;
import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.worker.service.WorkerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdvanceController.class)
class AdvanceControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvanceService advanceService;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @WithMockUser(roles = "ADMIN")
    @Test
    void advancesListShouldReturnOk() throws Exception {
        when(advanceService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/advances"))
            .andExpect(status().isOk())
            .andExpect(view().name("advances/list"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void advancesNewShouldReturnOk() throws Exception {
        when(workerService.findActive()).thenReturn(List.of());

        mockMvc.perform(get("/advances/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("advances/form"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void advancesCreateRouteShouldNotReturnNotFound() throws Exception {
        mockMvc.perform(post("/advances").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("advances/form"));
    }
}
