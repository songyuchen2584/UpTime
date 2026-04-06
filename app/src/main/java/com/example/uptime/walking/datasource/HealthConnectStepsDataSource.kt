package com.example.uptime.walking.datasource

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.uptime.walking.model.WalkingSessionInterval
import java.time.Instant

private const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"

class HealthConnectStepsDataSource(
    private val context: Context
) {
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
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

    fun permissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    suspend fun grantedPermissions(): Set<String> {
        val c = client ?: return emptySet()
        return c.permissionController.getGrantedPermissions()
    }

    fun installIntentIfNeeded(): Intent {
        val uriString =
            "market://details?id=$PROVIDER_PACKAGE_NAME&url=healthconnect%3A%2F%2Fonboarding"
        return Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uriString) }
    }

    suspend fun getTotalSteps(
        startMillis: Long,
        endMillis: Long
    ): Long {
        val c = client ?: return 0L
        val response = c.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(startMillis),
                    Instant.ofEpochMilli(endMillis)
                )
            )
        )
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }

    suspend fun getWalkingSessions(
        startMillis: Long,
        endMillis: Long
    ): List<WalkingSessionInterval> {
        val c = client ?: return emptyList()

        val response = c.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(startMillis),
                    Instant.ofEpochMilli(endMillis)
                )
            )
        )

        return response.records
            .filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_WALKING }
            .map {
                WalkingSessionInterval(
                    startMillis = it.startTime.toEpochMilli(),
                    endMillis = it.endTime.toEpochMilli()
                )
            }
    }
}