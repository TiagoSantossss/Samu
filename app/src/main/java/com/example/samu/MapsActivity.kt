package com.example.samu

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.MapView

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchViewOrigin: SearchView
    private lateinit var searchViewDestination: SearchView
    private lateinit var btnTraçarRota: Button
    private lateinit var mapView: MapView

    private var origemLatLng: LatLng? = null
    private var destinoLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Inicializa a MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Inicializa o serviço de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializa os campos de pesquisa e botão
        searchViewOrigin = findViewById(R.id.searchViewOrigin)
        searchViewDestination = findViewById(R.id.searchViewDestination)
        btnTraçarRota = findViewById(R.id.btnTraçarRota)

        setupSearch()
        btnTraçarRota.setOnClickListener { traçarRota() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                origemLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origemLatLng!!, 15f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permissão de localização necessária!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSearch() {
        searchViewOrigin.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    origemLatLng = searchLocation(query, "Origem")
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        searchViewDestination.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    destinoLatLng = searchLocation(query, "Destino")
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchLocation(location: String, tipo: String): LatLng? {
        val geocoder = Geocoder(this)
        return try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title("$tipo: $location"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                latLng
            } else {
                Toast.makeText(this, "$tipo não encontrado", Toast.LENGTH_SHORT).show()
                null
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Erro ao buscar localização", e)
            Toast.makeText(this, "Erro ao buscar localização", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun traçarRota() {
        if (origemLatLng == null || destinoLatLng == null) {
            Toast.makeText(this, "Selecione origem e destino", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpa o mapa antes de traçar a nova rota
        mMap.clear()

        // Adiciona marcadores
        mMap.addMarker(MarkerOptions().position(origemLatLng!!).title("Origem"))
        mMap.addMarker(MarkerOptions().position(destinoLatLng!!).title("Destino"))

        // Simula uma rota (substitua por API do Google Directions caso tenha uma chave válida)
        val polylineOptions = PolylineOptions()
            .add(origemLatLng!!)
            .add(destinoLatLng!!)
            .width(10f)
            .color(android.graphics.Color.BLUE)

        mMap.addPolyline(polylineOptions)

        // Centraliza a rota no mapa
        val bounds = LatLngBounds.Builder()
            .include(origemLatLng!!)
            .include(destinoLatLng!!)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
