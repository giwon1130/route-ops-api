package com.giwon.routeops.features.dashboard.presentation

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `snapshot endpoint returns dashboard payload`() {
        mockMvc.perform(get("/api/v1/dashboard/snapshot"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.summary.activeDemands").exists())
            .andExpect(jsonPath("$.data.zones[0].name").exists())
    }
}
