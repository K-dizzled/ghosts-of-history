package com.ghosts.of.history.persistentcloudanchor

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ghosts.of.history.R
import com.ghosts.of.history.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun addMarkers() {
        val sydney = LatLng(-34.0, 151.0)
        var marker = mMap.addMarker(MarkerOptions().position(sydney).title("Жопа Андрея").snippet("Сюда лучше не соваться"))
        marker?.tag = false
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        addMarkers()
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.0, 151.0)))
        mMap.setOnInfoWindowClickListener(InfoWindowActivity())
        mMap.setOnMarkerClickListener(this)
        mMap.setInfoWindowAdapter(InfoWindowAdapter())
    }

    /** Called when the user clicks a marker.  */
    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    internal inner class InfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        var mWindow: View = layoutInflater.inflate(R.layout.info_window, null)

        private fun setInfoWindowText(marker: Marker) {
            val tvTitle = mWindow.findViewById<TextView>(R.id.tvTitle)
            if (marker.tag == false) {
                tvTitle.text = marker.title
                marker.tag = true
            } else if (marker.tag == true){
                tvTitle.text = marker.snippet
                marker.tag = false
            }
        }

        override fun getInfoWindow(arg0: Marker): View? {
            setInfoWindowText(arg0)
            return mWindow
        }

        override fun getInfoContents(arg0: Marker): View? {
            setInfoWindowText(arg0)
            return mWindow
        }
    }
    internal inner class InfoWindowActivity : AppCompatActivity(),
            GoogleMap.OnInfoWindowClickListener,
            OnMapReadyCallback {
        override fun onMapReady(googleMap: GoogleMap) {
            // Add markers to the map and do other map setup.
            // ...
            // Set a listener for info window events0.
            googleMap.setOnInfoWindowClickListener(this)
        }

        override fun onInfoWindowClick(marker: Marker) {
            marker.showInfoWindow()
            println("Info Window Clicked")
        }
    }


}