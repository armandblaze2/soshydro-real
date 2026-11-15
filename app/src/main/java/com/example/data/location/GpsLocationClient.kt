package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.R
import com.example.data.model.GpsCaptureResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GpsLocationClient(private val context: Context) {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationServiceEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    @SuppressLint("MissingPermission")
    suspend fun captureHighPrecisionLocation(): Result<GpsCaptureResult> {
        if (!isLocationServiceEnabled()) {
            return Result.failure(
                IllegalStateException(context.getString(R.string.gps_disabled_error))
            )
        }
        if (!hasLocationPermission()) {
            return Result.failure(
                SecurityException(context.getString(R.string.gps_permission_denied))
            )
        }

        // 1. Try Play Services FusedLocationProviderClient with high accuracy
        val fusedLocation = withTimeoutOrNull(12_000L) {
            try {
                awaitFusedCurrentLocation()
            } catch (_: Exception) {
                null
            }
        }
        if (fusedLocation != null) {
            return Result.success(fusedLocation.toCaptureResult())
        }

        // 2. Fallback to Android framework LocationManager (GPS then Network)
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager != null) {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
            ).filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

            for (provider in providers) {
                val frameworkLocation = withTimeoutOrNull(8_000L) {
                    try {
                        awaitFrameworkCurrentLocation(manager, provider)
                    } catch (_: Exception) {
                        null
                    }
                }
                if (frameworkLocation != null) {
                    return Result.success(frameworkLocation.toCaptureResult())
                }
            }

            // 3. Last-resort fallback to recent cached location from Fused or LocationManager
            val lastFused = runCatching { awaitFusedLastLocation() }.getOrNull()
            if (lastFused != null) {
                return Result.success(lastFused.toCaptureResult())
            }
            for (provider in providers) {
                val lastKnown = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                if (lastKnown != null) {
                    return Result.success(lastKnown.toCaptureResult())
                }
            }
        }

        return Result.failure(
            IllegalStateException("Impossible d'obtenir un signal GPS. Déplacez-vous à découvert et réessayez.")
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFusedCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val cancellationTokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { cancellationTokenSource.cancel() }

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (cont.isActive) cont.resume(location)
            }.addOnFailureListener { exc ->
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFusedLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener { exc ->
                    if (cont.isActive) cont.resumeWithException(exc)
                }
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFrameworkCurrentLocation(
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        cont.invokeOnCancellation { cancellationSignal.cancel() }

        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            cancellationSignal,
            ContextCompat.getMainExecutor(context)
        ) { location ->
            if (cont.isActive) cont.resume(location)
        }
    }

    private fun Location.toCaptureResult(): GpsCaptureResult = GpsCaptureResult(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else 15f
    )
}
