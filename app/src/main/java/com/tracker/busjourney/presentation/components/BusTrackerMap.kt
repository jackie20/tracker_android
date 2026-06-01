package com.tracker.busjourney.presentation.components

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.tracker.busjourney.domain.model.BusPosition
import com.tracker.busjourney.domain.model.RouteStop

@Composable
fun BusTrackerMap(
    routeStops: List<RouteStop>,
    busPositions: List<BusPosition>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var googleMap: GoogleMap? by remember { mutableStateOf(null) }
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = {
            mapView.also {
                it.getMapAsync { gMap ->
                    gMap.uiSettings.apply {
                        isZoomControlsEnabled = true
                        isCompassEnabled = true
                    }
                    googleMap = gMap
                }
            }
        },
        modifier = modifier,
    )

    LaunchedEffect(googleMap, routeStops, busPositions) {
        val map = googleMap ?: return@LaunchedEffect
        map.clear()

        if (routeStops.isNotEmpty()) {
            drawRouteLine(map, routeStops)
            drawStopMarkers(map, routeStops)
            moveCameraToRoute(map, routeStops)
        }

        busPositions.forEach { bus -> drawBusMarker(map, bus) }
    }
}

private fun drawRouteLine(map: GoogleMap, stops: List<RouteStop>) {
    map.addPolyline(
        PolylineOptions()
            .addAll(stops.map { LatLng(it.lat, it.lon) })
            .width(8f)
            .color(AndroidColor.parseColor("#003688"))
            .geodesic(true),
    )
}

private fun drawStopMarkers(map: GoogleMap, stops: List<RouteStop>) {
    val first = stops.first()
    val last = stops.last()

    map.addMarker(
        MarkerOptions()
            .position(LatLng(first.lat, first.lon))
            .title(first.name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)),
    )
    map.addMarker(
        MarkerOptions()
            .position(LatLng(last.lat, last.lon))
            .title(last.name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)),
    )
}

private fun drawBusMarker(map: GoogleMap, bus: BusPosition) {
    val minutesAway = kotlin.math.ceil(bus.timeToStation / 60.0).toInt()
    map.addMarker(
        MarkerOptions()
            .position(LatLng(bus.lat, bus.lon))
            .title("Bus ${bus.vehicleId}")
            .snippet("$minutesAway min to ${bus.nextStopName}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)),
    )
}

private fun moveCameraToRoute(map: GoogleMap, stops: List<RouteStop>) {
    try {
        val bounds = LatLngBounds.Builder().apply {
            stops.forEach { include(LatLng(it.lat, it.lon)) }
        }.build()
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
    } catch (_: IllegalStateException) {
        val first = stops.first()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(first.lat, first.lon), 13f))
    }
}
