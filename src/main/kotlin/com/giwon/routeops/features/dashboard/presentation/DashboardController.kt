package com.giwon.routeops.features.dashboard.presentation

import com.giwon.routeops.common.response.ApiResponse
import com.giwon.routeops.features.dashboard.application.DashboardSimulationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class DashboardController(
    private val dashboardSimulationService: DashboardSimulationService
) {
    @GetMapping("/dashboard/summary")
    fun getSummary() = ApiResponse(data = dashboardSimulationService.getSnapshot().summary)

    @GetMapping("/dashboard/snapshot")
    fun getSnapshot() = ApiResponse(data = dashboardSimulationService.getSnapshot())

    @GetMapping("/zones")
    fun getZones() = ApiResponse(data = dashboardSimulationService.getSnapshot().zones)

    @GetMapping("/vehicles")
    fun getVehicles() = ApiResponse(data = dashboardSimulationService.getSnapshot().vehicles)

    @GetMapping("/demands")
    fun getDemands() = ApiResponse(data = dashboardSimulationService.getSnapshot().demands)

    @GetMapping("/alerts")
    fun getAlerts() = ApiResponse(data = dashboardSimulationService.getSnapshot().alerts)

    @GetMapping("/metrics/timeseries")
    fun getMetrics() = ApiResponse(data = dashboardSimulationService.getSnapshot().metrics)

    @GetMapping("/recommendations")
    fun getRecommendations() = ApiResponse(data = dashboardSimulationService.getSnapshot().recommendations)

    @GetMapping("/forecasts")
    fun getForecasts() = ApiResponse(data = dashboardSimulationService.getSnapshot().forecasts)

    @GetMapping("/copilot/briefing")
    fun getAiBriefing() = ApiResponse(data = dashboardSimulationService.getSnapshot().aiBriefing)

    @GetMapping("/what-if")
    fun getWhatIfScenarios() = ApiResponse(data = dashboardSimulationService.getSnapshot().whatIfScenarios)

    @PostMapping("/simulation/tick")
    fun advanceSimulation(): ApiResponse<Map<String, String>> {
        dashboardSimulationService.advance()
        return ApiResponse(data = mapOf("status" to "advanced"))
    }

    @PostMapping("/simulation/reset")
    fun resetSimulation(): ApiResponse<Map<String, String>> {
        dashboardSimulationService.reset()
        return ApiResponse(data = mapOf("status" to "reset"))
    }
}
