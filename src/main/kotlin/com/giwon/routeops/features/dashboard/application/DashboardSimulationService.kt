package com.giwon.routeops.features.dashboard.application

import com.giwon.routeops.features.dashboard.domain.AlertLevel
import com.giwon.routeops.features.dashboard.domain.AlertView
import com.giwon.routeops.features.dashboard.domain.DashboardSnapshot
import com.giwon.routeops.features.dashboard.domain.DashboardSummary
import com.giwon.routeops.features.dashboard.domain.DemandForecast
import com.giwon.routeops.features.dashboard.domain.DemandStatus
import com.giwon.routeops.features.dashboard.domain.DemandView
import com.giwon.routeops.features.dashboard.domain.GeoPoint
import com.giwon.routeops.features.dashboard.domain.MetricPoint
import com.giwon.routeops.features.dashboard.domain.RebalancingRecommendation
import com.giwon.routeops.features.dashboard.domain.ForecastTrend
import com.giwon.routeops.features.dashboard.domain.ScenarioStatus
import com.giwon.routeops.features.dashboard.domain.OperationsReport
import com.giwon.routeops.features.dashboard.domain.AiBriefing
import com.giwon.routeops.features.dashboard.domain.WhatIfScenario
import com.giwon.routeops.features.dashboard.domain.VehicleStatus
import com.giwon.routeops.features.dashboard.domain.VehicleHistory
import com.giwon.routeops.features.dashboard.domain.VehicleTimelinePoint
import com.giwon.routeops.features.dashboard.domain.VehicleView
import com.giwon.routeops.features.dashboard.domain.ZoneView
import com.giwon.routeops.features.dashboard.domain.ZoneTimeline
import com.giwon.routeops.features.dashboard.domain.ZoneTimelinePoint
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Service
class DashboardSimulationService {
    private var tick: Int = 0

    fun getSnapshot(): DashboardSnapshot {
        val scenario = currentScenario()
        val peakFactor = scenario.peakFactor
        val zones = buildZones(peakFactor)
        val vehicles = buildVehicles(peakFactor)
        val demands = buildDemands(peakFactor)
        val alerts = buildAlerts(zones)
        val metrics = buildMetrics()
        val recommendations = buildRecommendations(zones, peakFactor)
        val forecasts = buildForecasts(zones, peakFactor)
        val vehicleHistories = buildVehicleHistories(vehicles, scenario)
        val zoneTimelines = buildZoneTimelines(zones, scenario)
        val summary = DashboardSummary(
            activeDemands = demands.count { it.status != DemandStatus.MISSED },
            dispatchSuccessRate = 88.0 - (peakFactor - 1) * 4.5,
            averageWaitMinutes = 6.5 + peakFactor * 2.1,
            activeVehicles = vehicles.count { it.status != VehicleStatus.IDLE } + 4,
            shortageZones = zones.count { it.demandLevel >= 4 && it.availableVehicles <= 2 }
        )
        val report = buildOperationsReport(zones, recommendations, scenario)
        val aiBriefing = buildAiBriefing(zones, recommendations, forecasts, summary, scenario)
        val whatIfScenarios = buildWhatIfScenarios(summary, zones, recommendations, scenario)

        return DashboardSnapshot(
            scenario = ScenarioStatus(
                id = scenario.id,
                name = scenario.name,
                description = scenario.description,
                operationalFocus = scenario.operationalFocus,
                currentTime = scenario.currentTime
            ),
            summary = summary,
            zones = zones,
            vehicles = vehicles,
            demands = demands,
            alerts = alerts,
            metrics = metrics,
            recommendations = recommendations,
            forecasts = forecasts,
            vehicleHistories = vehicleHistories,
            zoneTimelines = zoneTimelines,
            report = report,
            aiBriefing = aiBriefing,
            whatIfScenarios = whatIfScenarios
        )
    }

    fun advance() {
        tick += 1
    }

    fun reset() {
        tick = 0
    }

    private fun buildZones(peakFactor: Int): List<ZoneView> {
        return listOf(
            ZoneView(
                id = "gangnam",
                name = "강남역 권역",
                demandLevel = minOf(5, 2 + peakFactor),
                availableVehicles = max(1, 5 - peakFactor),
                averageWaitMinutes = 6.5 + peakFactor * 1.9,
                inboundDemand = 18 + peakFactor * 4,
                outboundDemand = 12 + peakFactor * 3,
                centroid = GeoPoint(37.4981, 127.0276),
                polygon = rectangle(37.4952, 127.0222, 37.5015, 127.0334)
            ),
            ZoneView(
                id = "yeoksam",
                name = "역삼 업무 권역",
                demandLevel = minOf(5, 1 + peakFactor),
                availableVehicles = max(1, 4 - peakFactor / 2),
                averageWaitMinutes = 5.4 + peakFactor * 1.2,
                inboundDemand = 10 + peakFactor * 2,
                outboundDemand = 14 + peakFactor * 3,
                centroid = GeoPoint(37.5007, 127.0365),
                polygon = rectangle(37.4974, 127.0324, 37.5044, 127.0408)
            ),
            ZoneView(
                id = "samseong",
                name = "삼성 환승 권역",
                demandLevel = minOf(5, 2 + peakFactor / 2),
                availableVehicles = max(1, 3 - peakFactor / 2),
                averageWaitMinutes = 4.8 + peakFactor * 1.1,
                inboundDemand = 13 + peakFactor * 3,
                outboundDemand = 13 + peakFactor * 2,
                centroid = GeoPoint(37.5087, 127.0632),
                polygon = rectangle(37.5055, 127.0572, 37.5126, 127.0688)
            ),
            ZoneView(
                id = "jamsil",
                name = "잠실 주거 권역",
                demandLevel = minOf(5, 1 + (tick + 1) % 3),
                availableVehicles = 4,
                averageWaitMinutes = 4.2 + ((tick + 1) % 3) * 0.9,
                inboundDemand = 8 + (tick + 1) % 3 * 2,
                outboundDemand = 11 + (tick + 1) % 3 * 3,
                centroid = GeoPoint(37.5133, 127.1001),
                polygon = rectangle(37.5092, 127.0945, 37.5174, 127.1058)
            )
        )
    }

    private fun buildVehicles(peakFactor: Int): List<VehicleView> {
        return listOf(
            VehicleView("V-101", "강남 01", "gangnam", VehicleStatus.DISPATCHED, GeoPoint(37.4975, 127.0260), 80, "강남역 8번 출구 승차", 4),
            VehicleView("V-102", "강남 02", "gangnam", VehicleStatus.ON_TRIP, GeoPoint(37.4991, 127.0302), 65, "삼성 환승 권역 진입", 7),
            VehicleView("V-201", "역삼 01", "yeoksam", VehicleStatus.REBALANCING, GeoPoint(37.5012, 127.0381), 0, "강남역 권역 재배치", 6),
            VehicleView("V-202", "역삼 02", "yeoksam", if (peakFactor >= 3) VehicleStatus.DISPATCHED else VehicleStatus.IDLE, GeoPoint(37.5022, 127.0344), if (peakFactor >= 3) 55 else 0, if (peakFactor >= 3) "역삼 오피스 밀집 구간 진입" else "대기", if (peakFactor >= 3) 5 else 1),
            VehicleView("V-301", "삼성 01", "samseong", VehicleStatus.ON_TRIP, GeoPoint(37.5098, 127.0613), 70, "코엑스 북문 하차 예정", 3),
            VehicleView("V-401", "잠실 01", "jamsil", VehicleStatus.IDLE, GeoPoint(37.5142, 127.0984), 0, "잠실 주거 권역 대기", 1)
        )
    }

    private fun buildDemands(peakFactor: Int): List<DemandView> {
        val baseTime = LocalTime.of(18, 0).plusMinutes((tick * 5).toLong())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return listOf(
            DemandView("D-001", "gangnam", baseTime.minusMinutes(7).format(formatter), 11 + peakFactor, DemandStatus.WAITING, GeoPoint(37.4978, 127.0284), GeoPoint(37.5084, 127.0638)),
            DemandView("D-002", "gangnam", baseTime.minusMinutes(4).format(formatter), 6 + peakFactor, DemandStatus.MATCHED, GeoPoint(37.4989, 127.0252), GeoPoint(37.5134, 127.1011)),
            DemandView("D-003", "yeoksam", baseTime.minusMinutes(5).format(formatter), 9, DemandStatus.WAITING, GeoPoint(37.5016, 127.0362), GeoPoint(37.4903, 127.0306)),
            DemandView("D-004", "samseong", baseTime.minusMinutes(3).format(formatter), 5, DemandStatus.MATCHED, GeoPoint(37.5085, 127.0602), GeoPoint(37.5059, 127.0494)),
            DemandView("D-005", "jamsil", baseTime.minusMinutes(8).format(formatter), 13, if (peakFactor >= 3) DemandStatus.MISSED else DemandStatus.WAITING, GeoPoint(37.5143, 127.1012), GeoPoint(37.5218, 127.1196))
        )
    }

    private fun buildAlerts(zones: List<ZoneView>): List<AlertView> {
        val gangnam = zones.first { it.id == "gangnam" }
        val yeoksam = zones.first { it.id == "yeoksam" }
        return listOf(
            AlertView(
                id = "A-001",
                level = if (gangnam.demandLevel >= 4) AlertLevel.CRITICAL else AlertLevel.WARNING,
                zoneId = gangnam.id,
                title = "강남역 권역 수요 급증",
                message = "최근 10분 호출이 가용 차량 대비 2배 이상 증가했습니다.",
                action = "역삼 권역 유휴 차량 1~2대 재배치"
            ),
            AlertView(
                id = "A-002",
                level = AlertLevel.WARNING,
                zoneId = yeoksam.id,
                title = "역삼 권역 대기시간 상승",
                message = "평균 대기시간이 10분을 초과했습니다.",
                action = "강남역 승차 포인트 일부 분산 유도"
            ),
            AlertView(
                id = "A-003",
                level = AlertLevel.INFO,
                zoneId = "samseong",
                title = "삼성 환승 수요 증가 예상",
                message = "퇴근 피크 영향으로 30분 내 환승 호출 증가가 예상됩니다.",
                action = "삼성 권역 대기 차량 유지"
            )
        )
    }

    private fun buildMetrics(): List<MetricPoint> {
        val base = LocalTime.of(17, 30)
        return (0..5).map { index ->
            val demandCount = 18 + index * 4 + (tick % 3)
            MetricPoint(
                time = base.plusMinutes((index * 10).toLong()).format(DateTimeFormatter.ofPattern("HH:mm")),
                demandCount = demandCount,
                averageWaitMinutes = 5.5 + index * 1.1 + (tick % 2),
                activeVehicles = 8 + minOf(index, 3)
            )
        }
    }

    private fun buildRecommendations(zones: List<ZoneView>, peakFactor: Int): List<RebalancingRecommendation> {
        val gangnam = zones.first { it.id == "gangnam" }
        val yeoksam = zones.first { it.id == "yeoksam" }
        return listOf(
            RebalancingRecommendation(
                id = "R-001",
                fromZoneId = "yeoksam",
                toZoneId = "gangnam",
                title = "역삼 -> 강남역 권역 재배치",
                reason = "강남역 권역의 수요 레벨이 ${gangnam.demandLevel}까지 상승했고 대기시간이 빠르게 증가하고 있습니다.",
                ruleBasis = "최근 10분 호출 / 가용 차량 비율 > 2.0, 평균 대기시간 > 8분",
                suggestedVehicleCount = if (peakFactor >= 3) 2 else 1,
                expectedWaitReductionMinutes = if (peakFactor >= 3) 4.5 else 2.3,
                priority = if (peakFactor >= 3) AlertLevel.CRITICAL else AlertLevel.WARNING
            ),
            RebalancingRecommendation(
                id = "R-002",
                fromZoneId = "jamsil",
                toZoneId = "samseong",
                title = "잠실 대기 차량 일부 삼성 유지",
                reason = "삼성 환승 수요가 증가하는 시간대이며 잠실 권역은 현재 공급 여유가 있습니다.",
                ruleBasis = "환승 권역 수요 증가 + 주거 권역 유휴 차량 1대 이상",
                suggestedVehicleCount = 1,
                expectedWaitReductionMinutes = 1.8,
                priority = AlertLevel.INFO
            ),
            RebalancingRecommendation(
                id = "R-003",
                fromZoneId = "jamsil",
                toZoneId = "yeoksam",
                title = "심야 전환 전 역삼 대기 확보",
                reason = "역삼 업무 권역 대기시간이 ${if (peakFactor >= 2) "상승 추세" else "안정적"}지만 30분 후 피크 대비가 필요합니다.",
                ruleBasis = "예측 호출량 증가 + 회차 가능 차량 1대 이상",
                suggestedVehicleCount = if (yeoksam.availableVehicles <= 2) 2 else 1,
                expectedWaitReductionMinutes = 2.0 + peakFactor * 0.4,
                priority = AlertLevel.WARNING
            )
        )
    }

    private fun buildForecasts(zones: List<ZoneView>, peakFactor: Int): List<DemandForecast> {
        return zones.map { zone ->
            val nextDemand = zone.inboundDemand + zone.outboundDemand + peakFactor * 2
            val confidence = when (zone.id) {
                "gangnam" -> 89
                "samseong" -> 82
                "yeoksam" -> 78
                else -> 74
            }
            val action = when (zone.id) {
                "gangnam" -> "강남역 승차 포인트 분산 안내 + 역삼 유휴 차량 선반영"
                "yeoksam" -> "오피스 퇴근 피크 대비 대기 차량 1대 유지"
                "samseong" -> "환승 수요 대응용 차량 회차 시간 단축"
                else -> "잠실 권역 대기 차량 유지 후 필요 시 삼성 이동"
            }
            DemandForecast(
                zoneId = zone.id,
                zoneName = zone.name,
                next30MinutesDemand = nextDemand,
                expectedWaitMinutes = zone.averageWaitMinutes + peakFactor * 0.8,
                confidence = confidence,
                trend = when {
                    zone.id == "gangnam" || zone.id == "samseong" -> ForecastTrend.UP
                    zone.id == "jamsil" && peakFactor <= 2 -> ForecastTrend.STEADY
                    else -> if (peakFactor >= 3) ForecastTrend.UP else ForecastTrend.STEADY
                },
                driverAction = action
            )
        }
    }

    private fun buildVehicleHistories(vehicles: List<VehicleView>, scenario: SimulationScenario): List<VehicleHistory> {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val baseTime = LocalTime.parse(scenario.currentTime, formatter)
        return vehicles.mapIndexed { index, vehicle ->
            val previousStatus = when (vehicle.status) {
                VehicleStatus.DISPATCHED -> VehicleStatus.IDLE
                VehicleStatus.ON_TRIP -> VehicleStatus.DISPATCHED
                VehicleStatus.REBALANCING -> VehicleStatus.IDLE
                VehicleStatus.IDLE -> VehicleStatus.IDLE
            }
            val previousZone = when (vehicle.zoneId) {
                "gangnam" -> "yeoksam"
                "samseong" -> "gangnam"
                "jamsil" -> "samseong"
                else -> "yeoksam"
            }
            VehicleHistory(
                vehicleId = vehicle.id,
                vehicleLabel = vehicle.label,
                points = listOf(
                    VehicleTimelinePoint(baseTime.minusMinutes((20 + index).toLong()).format(formatter), previousZone, previousStatus),
                    VehicleTimelinePoint(baseTime.minusMinutes((10 + index).toLong()).format(formatter), vehicle.zoneId, previousStatus),
                    VehicleTimelinePoint(baseTime.format(formatter), vehicle.zoneId, vehicle.status)
                )
            )
        }
    }

    private fun buildZoneTimelines(zones: List<ZoneView>, scenario: SimulationScenario): List<ZoneTimeline> {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val baseTime = LocalTime.parse(scenario.currentTime, formatter)
        return zones.map { zone ->
            ZoneTimeline(
                zoneId = zone.id,
                zoneName = zone.name,
                points = listOf(
                    ZoneTimelinePoint(
                        time = baseTime.minusMinutes(20).format(formatter),
                        demandLevel = max(1, zone.demandLevel - 1),
                        availableVehicles = zone.availableVehicles + 1,
                        averageWaitMinutes = max(3.0, zone.averageWaitMinutes - 1.4)
                    ),
                    ZoneTimelinePoint(
                        time = baseTime.minusMinutes(10).format(formatter),
                        demandLevel = zone.demandLevel,
                        availableVehicles = zone.availableVehicles,
                        averageWaitMinutes = max(3.0, zone.averageWaitMinutes - 0.6)
                    ),
                    ZoneTimelinePoint(
                        time = baseTime.format(formatter),
                        demandLevel = zone.demandLevel,
                        availableVehicles = zone.availableVehicles,
                        averageWaitMinutes = zone.averageWaitMinutes
                    )
                )
            )
        }
    }

    private fun buildOperationsReport(
        zones: List<ZoneView>,
        recommendations: List<RebalancingRecommendation>,
        scenario: SimulationScenario
    ): OperationsReport {
        val topZone = zones.maxBy { it.demandLevel * 10 + it.averageWaitMinutes }
        return OperationsReport(
            generatedAt = scenario.currentTime,
            headline = "${scenario.name} 기준 ${topZone.name} 대응이 우선입니다.",
            highlights = listOf(
                "${topZone.name} 평균 대기시간 ${topZone.averageWaitMinutes.toInt()}분 수준",
                "현재 재배치 추천 ${recommendations.size}건 활성",
                "업무/환승 권역 중심 수요 집중 유지"
            ),
            risks = listOf(
                "가용 차량 2대 이하 권역에서 지연 누적 가능",
                "회차 지연 시 다음 배차 응답성 저하 가능"
            ),
            operatorActions = recommendations.take(3).map { "${it.title}: ${it.suggestedVehicleCount}대 조정" }
        )
    }

    private fun buildAiBriefing(
        zones: List<ZoneView>,
        recommendations: List<RebalancingRecommendation>,
        forecasts: List<DemandForecast>,
        summary: DashboardSummary,
        scenario: SimulationScenario
    ): AiBriefing {
        val topZone = zones.maxBy { it.demandLevel * 10 + it.averageWaitMinutes }
        val topRecommendation = recommendations.maxBy { it.expectedWaitReductionMinutes + it.suggestedVehicleCount }
        val risingForecast = forecasts
            .filter { it.trend == ForecastTrend.UP }
            .maxByOrNull { it.next30MinutesDemand }

        return AiBriefing(
            headline = "${topZone.name} 우선 대응이 필요한 상태입니다.",
            overview = "${scenario.name} 영향으로 활성 호출 ${summary.activeDemands}건, 평균 대기 ${summary.averageWaitMinutes.toInt()}분 수준까지 올라왔습니다.",
            recommendedAction = topRecommendation.title,
            rationale = listOf(
                "${topZone.name}의 수요 레벨은 ${topZone.demandLevel}, 가용 차량은 ${topZone.availableVehicles}대입니다.",
                "${topRecommendation.suggestedVehicleCount}대 재배치 시 대기시간 ${topRecommendation.expectedWaitReductionMinutes.toFixed(1)}분 감소가 예상됩니다.",
                risingForecast?.let { "${it.zoneName}는 30분 내 ${it.next30MinutesDemand}건 수준으로 추가 상승 가능성이 있습니다." }
                    ?: "주요 환승 권역 수요는 당분간 상승 추세입니다."
            ),
            expectedImpact = "즉시 재배치 후 배차 성공률을 ${minOf(96.0, summary.dispatchSuccessRate + 2.8).toFixed(1)}% 수준까지 방어하는 것을 목표로 합니다.",
            confidence = 84 + scenario.peakFactor * 3
        )
    }

    private fun buildWhatIfScenarios(
        summary: DashboardSummary,
        zones: List<ZoneView>,
        recommendations: List<RebalancingRecommendation>,
        scenario: SimulationScenario
    ): List<WhatIfScenario> {
        val gangnam = zones.first { it.id == "gangnam" }
        val samseong = zones.first { it.id == "samseong" }

        return recommendations.mapIndexed { index, recommendation ->
            val waitReduction = recommendation.expectedWaitReductionMinutes + index * 0.3
            val dispatchGain = 1.8 + index * 0.7 + scenario.peakFactor * 0.4
            val focusZone = if (recommendation.toZoneId == "gangnam") gangnam else samseong

            WhatIfScenario(
                id = "W-${index + 1}",
                title = "${focusZone.name} 대응 시뮬레이션 ${index + 1}",
                basedOnRecommendationId = recommendation.id,
                summary = recommendation.reason,
                vehicleShift = "${recommendation.fromZoneId} -> ${recommendation.toZoneId} ${recommendation.suggestedVehicleCount}대 이동",
                baselineWaitMinutes = summary.averageWaitMinutes,
                projectedWaitMinutes = max(3.2, summary.averageWaitMinutes - waitReduction),
                baselineDispatchSuccessRate = summary.dispatchSuccessRate,
                projectedDispatchSuccessRate = minOf(97.5, summary.dispatchSuccessRate + dispatchGain),
                riskLevel = recommendation.priority,
                tradeOff = when (recommendation.priority) {
                    AlertLevel.CRITICAL -> "원본 권역 공급 여유가 빠르게 줄 수 있어 후속 회차 관리가 필요합니다."
                    AlertLevel.WARNING -> "예상 효과는 안정적이지만, 반대 권역 호출 증가 시 추가 재배치가 필요할 수 있습니다."
                    AlertLevel.INFO -> "선제 대응 성격이 강해 즉각 체감 효과는 작지만 피크 분산에 유리합니다."
                }
            )
        }
    }

    private fun currentScenario(): SimulationScenario {
        return when (tick % 4) {
            0 -> SimulationScenario(
                id = "commute-evening",
                name = "퇴근 피크",
                description = "강남·역삼 업무 권역에서 주거지 방향 호출이 빠르게 증가하는 상황입니다.",
                operationalFocus = "업무 권역 -> 주거 권역 이동 수요 대응",
                currentTime = "18:10",
                peakFactor = 1
            )
            1 -> SimulationScenario(
                id = "rainy-evening",
                name = "우천 저녁 피크",
                description = "우천 영향으로 단거리 호출과 환승 회피 수요가 함께 증가하는 상황입니다.",
                operationalFocus = "승차 포인트 분산과 회차 시간 단축",
                currentTime = "18:35",
                peakFactor = 2
            )
            2 -> SimulationScenario(
                id = "event-dispersal",
                name = "행사 종료 분산",
                description = "코엑스 인근 행사 종료로 삼성 환승 권역과 강남역 권역에 수요가 급증한 상황입니다.",
                operationalFocus = "환승 권역 집중 대응과 유휴 차량 재배치",
                currentTime = "19:05",
                peakFactor = 3
            )
            else -> SimulationScenario(
                id = "late-night-transition",
                name = "심야 전환 구간",
                description = "퇴근 피크 이후에도 일부 업무 권역과 환승 권역에 잔여 수요가 남아 있는 상황입니다.",
                operationalFocus = "공차 유지 최소화와 다음 권역 선배치",
                currentTime = "21:10",
                peakFactor = 2
            )
        }
    }

    private fun rectangle(
        south: Double,
        west: Double,
        north: Double,
        east: Double
    ): List<GeoPoint> {
        return listOf(
            GeoPoint(south, west),
            GeoPoint(south, east),
            GeoPoint(north, east),
            GeoPoint(north, west)
        )
    }

    private data class SimulationScenario(
        val id: String,
        val name: String,
        val description: String,
        val operationalFocus: String,
        val currentTime: String,
        val peakFactor: Int
    )

    private fun Double.toFixed(scale: Int): String = "%.${scale}f".format(this)
}
