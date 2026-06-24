package com.workerpay.advance.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.workerpay.advance.service.AdvanceService;
import com.workerpay.auth.service.CustomUserDetailsService;
import com.workerpay.config.SecurityConfig;
import com.workerpay.worker.service.WorkerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdvanceController.class)
@Import(SecurityConfig.class)
class AdvanceControllerWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvanceService advanceService;

    @MockitoBean
    private WorkerService workerService;

    @MockitoBean
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

    @WithMockUser(roles = "OPERATOR")
    @Test
    void operatorCanListAdvances() throws Exception {
        when(advanceService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/advances"))
            .andExpect(status().isOk())
            .andExpect(view().name("advances/list"));
    }

    @WithMockUser(roles = "OPERATOR")
    @Test
    void operatorCannotOpenAdvanceForm() throws Exception {
        mockMvc.perform(get("/advances/new"))
            .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "OPERATOR")
    @Test
    void operatorCannotCreateAdvance() throws Exception {
        mockMvc.perform(post("/advances").with(csrf()))
            .andExpect(status().isForbidden());
    }
}
