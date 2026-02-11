package com.example.healthconnect20

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"

data class ExerciseSessionUi(
    val start: Instant,
    val end: Instant,
    val title: String
)

class HealthConnectRepository(private val context: Context) {

    // Permissions we will request
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    private val client: HealthConnectClient? by lazy {
        val status = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)
        if (status == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    fun sdkStatus(): Int =
        HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)

    fun installIntentIfNeeded(): Intent {
        // From the official guide: redirect user to install / update the provider
        // market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding
        val uriString =
            "market://details?id=$PROVIDER_PACKAGE_NAME&url=healthconnect%3A%2F%2Fonboarding"
        return Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uriString) }
    }

    suspend fun grantedPermissions(): Set<String> {
        val c = client ?: return emptySet()
        return c.permissionController.getGrantedPermissions()
    }

    fun permissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    suspend fun readTotalStepsLast24h(): Long {
        val c = client ?: return 0L
        val end = Instant.now()
        val start = end.minus(24, ChronoUnit.HOURS)

        val response: AggregationResult = c.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun readExerciseSessionsLast7d(): List<ExerciseSessionUi> {
        val c = client ?: return emptyList()
        val end = Instant.now()
        val start = end.minus(7, ChronoUnit.DAYS)

        val records = c.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

        return records
            .sortedByDescending { it.startTime }
            .map { rec ->
                ExerciseSessionUi(
                    start = rec.startTime,
                    end = rec.endTime,
                    title = rec.exerciseType.toString()
                )
            }
    }
}
