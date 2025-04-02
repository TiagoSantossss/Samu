package com.example.samu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView
    private var selectedLocation: LatLng? = null  // Guarda o local pesquisado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar a barra de pesquisa
        searchView = findViewById(R.id.search_location)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Configurar botão para abrir a página de rotas
        findViewById<android.widget.Button>(R.id.btn_view_routes).setOnClickListener {
            openRoutesActivity()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verificar permissões
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                mMap.addMarker(MarkerOptions().position(userLocation).title("Minha localização"))
            }
        }
    }

    // Função para pesquisar localização e definir como destino
    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList = geocoder.getFromLocationName(locationName, 1)

        if (!addressList.isNullOrEmpty()) {
            val address = addressList[0]
            selectedLocation = LatLng(address.latitude, address.longitude)

            mMap.clear()
            mMap.addMarker(MarkerOptions().position(selectedLocation!!).title(locationName))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation!!, 15f))
        } else {
            Toast.makeText(this, "Local não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    // Abre a atividade de trajetos passando as coordenadas
    private fun openRoutesActivity() {
        selectedLocation?.let {
            val intent = Intent(this, RoutesActivity::class.java)
            intent.putExtra("lat", it.latitude)
            intent.putExtra("lng", it.longitude)
            startActivity(intent)
        } ?: Toast.makeText(this, "Nenhuma localização selecionada!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap)
        }
    }
}
