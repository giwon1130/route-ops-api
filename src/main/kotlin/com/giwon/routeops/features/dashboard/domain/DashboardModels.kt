package com.giwon.routeops.features.dashboard.domain

data class DashboardSummary(
    val activeDemands: Int,
    val dispatchSuccessRate: Double,
    val averageWaitMinutes: Double,
    val activeVehicles: Int,
    val shortageZones: Int
)

data class ZoneView(
    val id: String,
    val name: String,
    val demandLevel: Int,
    val availableVehicles: Int,
    val averageWaitMinutes: Double,
    val inboundDemand: Int,
    val outboundDemand: Int,
    val centroid: GeoPoint,
    val polygon: List<GeoPoint>
)

data class VehicleView(
    val id: String,
    val label: String,
    val zoneId: String,
    val status: VehicleStatus,
    val location: GeoPoint,
    val occupancyRate: Int,
    val nextAction: String,
    val estimatedReadyMinutes: Int
)

data class DemandView(
    val id: String,
    val zoneId: String,
    val requestedAt: String,
    val waitMinutes: Int,
    val status: DemandStatus,
    val pickup: GeoPoint,
    val dropoff: GeoPoint
)

data class AlertView(
    val id: String,
    val level: AlertLevel,
    val zoneId: String?,
    val title: String,
    val message: String,
    val action: String
)

data class MetricPoint(
    val time: String,
    val demandCount: Int,
    val averageWaitMinutes: Double,
    val activeVehicles: Int
)

data class RebalancingRecommendation(
    val id: String,
    val fromZoneId: String,
    val toZoneId: String,
    val title: String,
    val reason: String,
    val ruleBasis: String,
    val suggestedVehicleCount: Int,
    val expectedWaitReductionMinutes: Double,
    val priority: AlertLevel
)

data class DemandForecast(
    val zoneId: String,
    val zoneName: String,
    val next30MinutesDemand: Int,
    val expectedWaitMinutes: Double,
    val confidence: Int,
    val trend: ForecastTrend,
    val driverAction: String
)

data class ScenarioStatus(
    val id: String,
    val name: String,
    val description: String,
    val operationalFocus: String,
    val currentTime: String
)

data class VehicleTimelinePoint(
    val time: String,
    val zoneId: String,
    val status: VehicleStatus
)

data class VehicleHistory(
    val vehicleId: String,
    val vehicleLabel: String,
    val points: List<VehicleTimelinePoint>
)

data class ZoneTimelinePoint(
    val time: String,
    val demandLevel: Int,
    val availableVehicles: Int,
    val averageWaitMinutes: Double
)

data class ZoneTimeline(
    val zoneId: String,
    val zoneName: String,
    val points: List<ZoneTimelinePoint>
)

data class OperationsReport(
    val generatedAt: String,
    val headline: String,
    val highlights: List<String>,
    val risks: List<String>,
    val operatorActions: List<String>
)

data class AiBriefing(
    val headline: String,
    val overview: String,
    val recommendedAction: String,
    val rationale: List<String>,
    val expectedImpact: String,
    val confidence: Int
)

data class WhatIfScenario(
    val id: String,
    val title: String,
    val basedOnRecommendationId: String,
    val summary: String,
    val vehicleShift: String,
    val baselineWaitMinutes: Double,
    val projectedWaitMinutes: Double,
    val baselineDispatchSuccessRate: Double,
    val projectedDispatchSuccessRate: Double,
    val riskLevel: AlertLevel,
    val tradeOff: String
)

data class DashboardSnapshot(
    val scenario: ScenarioStatus,
    val summary: DashboardSummary,
    val zones: List<ZoneView>,
    val vehicles: List<VehicleView>,
    val demands: List<DemandView>,
    val alerts: List<AlertView>,
    val metrics: List<MetricPoint>,
    val recommendations: List<RebalancingRecommendation>,
    val forecasts: List<DemandForecast>,
    val vehicleHistories: List<VehicleHistory>,
    val zoneTimelines: List<ZoneTimeline>,
    val report: OperationsReport,
    val aiBriefing: AiBriefing,
    val whatIfScenarios: List<WhatIfScenario>
)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

enum class VehicleStatus {
    IDLE,
    DISPATCHED,
    ON_TRIP,
    REBALANCING
}

enum class DemandStatus {
    WAITING,
    MATCHED,
    MISSED
}

enum class AlertLevel {
    INFO,
    WARNING,
    CRITICAL
}

enum class ForecastTrend {
    UP,
    STEADY,
    DOWN
}
