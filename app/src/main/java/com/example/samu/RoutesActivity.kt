package com.example.samu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class RoutesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var destination: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)

        // Obter coordenadas do Intent
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)
        if (lat != 0.0 && lng != 0.0) {
            destination = LatLng(lat, lng)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_routes) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        destination?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Destino"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))

            // Exemplo de uma linha ligando um ponto de origem a um destino (pode ser atualizado com API de rotas)
            val origin = LatLng(-23.55052, -46.633308) // Exemplo: São Paulo
            mMap.addPolyline(PolylineOptions().add(origin, it).width(5f).color(android.graphics.Color.BLUE))
        }
    }
}
