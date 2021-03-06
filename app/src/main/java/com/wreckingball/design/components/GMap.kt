package com.wreckingball.design.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.wreckingball.design.callbacks.MapsCallback
import com.wreckingball.design.models.Sign
import com.wreckingball.design.utils.PreferencesWrapper
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class GMap(campaignSigns: CampaignSigns) : KoinComponent {
    private val preferencesWrapper: PreferencesWrapper by inject()
    private var map: GoogleMap? = null
    lateinit var mapsCallback: MapsCallback
    private val campaignRepository = campaignSigns.campaignRepository
    private val signRepository = campaignSigns.signRepository

    fun initializeMap(context: Context,
                      fusedLocationProviderClient: FusedLocationProviderClient,
                      googleMap: GoogleMap?) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        map = googleMap

        //get saved camera position
        val camPositionJson = preferencesWrapper.getString("cameraPosition", "")
        var camPosition: CameraPosition? = null
        if (camPositionJson.isNotEmpty()) {
            camPosition = Gson().fromJson(camPositionJson, CameraPosition::class.java)
        }

        if (camPosition != null) {
            map?.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition))
        } else {
            //initialize camera position to users current location
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                }
            }
        }

        //display zoom controls
        map?.uiSettings?.isZoomControlsEnabled = true

        restoreMarkers()

        map?.setOnMapClickListener { latLng ->
            mapsCallback.onNewMarker(latLng)
        }

        map?.setOnMarkerClickListener {marker ->
            val clickedSign = signRepository.getSign(marker.tag as String)
            clickedSign?.let {sign ->
                mapsCallback.onMarkerClicked(sign)
            }
            true
        }
    }

    fun saveState() {
        val camPosJson = Gson().toJson(map?.cameraPosition)
        preferencesWrapper.putString("cameraPosition", camPosJson)
    }

    fun addMarker(latLng: LatLng, numMarkers: Int) {
        val title = "Sign Location ${signRepository.getNumSigns() + 1}"
        val campaign = campaignRepository.getCurrentCampaign()
        val campaignId = campaign?.id
        if (!campaignId.isNullOrEmpty()) {
            val newMarker = map?.addMarker(MarkerOptions().position(latLng).title(title))
            newMarker?.let {marker ->
                val id = UUID.randomUUID().toString()
                marker.tag = id
                val sign = Sign(
                    marker,
                    id,
                    title,
                    "",
                    latLng.latitude,
                    latLng.longitude,
                    numMarkers,
                    campaignId
                )
                signRepository.addNewSign(sign)
            }
        } else {
            //TODO: Add error handling
        }
    }

    fun clearMarkers() {
        map?.clear()
    }

    fun removeMarker(sign: Sign) {
        signRepository.deleteSign(sign.markerId)
        sign.marker.remove()
    }

    private fun restoreMarkers() {
        clearMarkers()
        val signs = signRepository.campaignSigns.value
        signs?.forEach { sign ->
            val newMarker = map?.addMarker(MarkerOptions().position(LatLng(sign.latitude, sign.longitude)).title(sign.title))
            newMarker?.let {marker ->
                marker.tag = sign.markerId
                sign.marker = marker
            }
        }
    }
}
