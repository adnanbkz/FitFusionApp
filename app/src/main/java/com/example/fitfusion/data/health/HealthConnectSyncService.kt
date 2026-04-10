package com.example.fitfusion.data.health

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

data class DailyHealthData(
    val date: String,
    val steps: Long,
    val stepCaloriesEstimated: Int,
    val averageHeartRate: Long?,
    val source: String = "health_connect",
)

class HealthConnectSyncService(
    private val client: HealthConnectClient
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun readDailyData(date: LocalDate = LocalDate.now()): DailyHealthData {
        val zone = ZoneId.systemDefault()
        val startInstant = date.atStartOfDay(zone).toInstant()
        val endInstant = date.plusDays(1).atStartOfDay(zone).toInstant()
        val timeRange = TimeRangeFilter.between(startInstant, endInstant)

        val stepsResponse = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = timeRange,
            )
        )
        val totalSteps = stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L

        val avgHR = readAverageHeartRate(timeRange)

        val stepCalories = (totalSteps * 0.04).toInt()

        return DailyHealthData(
            date = date.toString(),
            steps = totalSteps,
            stepCaloriesEstimated = stepCalories,
            averageHeartRate = avgHR,
        )
    }

    private suspend fun readAverageHeartRate(timeRange: TimeRangeFilter): Long? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = timeRange,
                )
            )
            if (response.records.isEmpty()) return null

            val allSamples = response.records.flatMap { it.samples }
            if (allSamples.isEmpty()) return null

            allSamples.map { it.beatsPerMinute }.average().toLong()
        } catch (e: Exception) {
            null // Si falla, simplemente no incluimos HR
        }
    }
}