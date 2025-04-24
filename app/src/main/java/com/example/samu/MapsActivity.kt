package com.example.samu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.common.api.Status


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvRouteInfo: TextView
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var token: AutocompleteSessionToken
    private var originName: String? = null
    private var destinationName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val btnVerRotas = findViewById<Button>(R.id.btn_view_routes)
        btnVerRotas.setOnClickListener {
            val intent = Intent(this@MapsActivity, VerRotas::class.java)
            originName?.let {
                intent.putExtra("origin_name", it)
            }
            destinationName?.let {
                intent.putExtra("dest_name", it)
            }
            startActivity(intent)
        }



        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("origin_lat") && savedInstanceState.containsKey("origin_lng")) {
                val lat = savedInstanceState.getDouble("origin_lat")
                val lng = savedInstanceState.getDouble("origin_lng")
                originLatLng = LatLng(lat, lng)
            }
            if (savedInstanceState.containsKey("dest_lat") && savedInstanceState.containsKey("dest_lng")) {
                val lat = savedInstanceState.getDouble("dest_lat")
                val lng = savedInstanceState.getDouble("dest_lng")
                destinationLatLng = LatLng(lat, lng)
            }
        }

        // Inicializar Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDdxlVIO3mbaFrf9g3PxnJWqL0WiJBG578", Locale.getDefault())
        }

        placesClient = Places.createClient(this)
        token = AutocompleteSessionToken.newInstance()

        val autocompleteOrigin =
            supportFragmentManager.findFragmentById(R.id.autocomplete_origin) as AutocompleteSupportFragment
        val autocompleteDestination =
            supportFragmentManager.findFragmentById(R.id.autocomplete_destination) as AutocompleteSupportFragment

        autocompleteOrigin.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteDestination.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteOrigin.setHint("Origem")
        autocompleteDestination.setHint("Destino")

        autocompleteOrigin.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                originLatLng = place.latLng
                originName = place.name
                mMap.addMarker(MarkerOptions().position(originLatLng!!).title("Origem: ${place.name}"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng!!, 12f))
                checkDrawRoute()
            }

            override fun onError(status: Status) {
                Toast.makeText(this@MapsActivity, "Erro: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        autocompleteDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationLatLng = place.latLng
                destinationName = place.name
                mMap.addMarker(MarkerOptions().position(destinationLatLng!!).title("Destino: ${place.name}"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng!!, 12f))
                checkDrawRoute()
            }

            override fun onError(status: Status) {
                Toast.makeText(this@MapsActivity, "Erro: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Customizar visual dos campos
        val originView = autocompleteOrigin.view
        val destView = autocompleteDestination.view

        originView?.post {
            originView.setBackgroundColor(Color.WHITE)
            originView.setPadding(20, 20, 20, 20)
        }

        destView?.post {
            destView.setBackgroundColor(Color.WHITE)
            destView.setPadding(20, 20, 20, 20)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                originLatLng = LatLng(it.latitude, it.longitude)
                if (destinationLatLng != null) {
                    checkDrawRoute()
                }


                // Adiciona marcador e move a câmara logo no início
                mMap.addMarker(MarkerOptions().position(originLatLng!!).title("Minha localização"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng!!, 15f))

                // Preenche automaticamente o campo de origem
                val autocompleteOrigin =
                    supportFragmentManager.findFragmentById(R.id.autocomplete_origin) as AutocompleteSupportFragment
                autocompleteOrigin.setText("Localização Atual")
            }
        }
        originLatLng?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Origem"))
        }
        destinationLatLng?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Destino"))
        }

        if (originLatLng != null && destinationLatLng != null) {
            checkDrawRoute()
        } else if (originLatLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng!!, 15f))
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        originLatLng?.let {
            outState.putDouble("origin_lat", it.latitude)
            outState.putDouble("origin_lng", it.longitude)
        }
        destinationLatLng?.let {
            outState.putDouble("dest_lat", it.latitude)
            outState.putDouble("dest_lng", it.longitude)
        }
    }


    private fun checkDrawRoute() {
        if (originLatLng != null && destinationLatLng != null) {
            drawRouteOnMap(originLatLng!!, destinationLatLng!!)
        }
    }

    private fun drawRouteOnMap(origin: LatLng, destination: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=transit" +
                "&key=AIzaSyDdxlVIO3mbaFrf9g3PxnJWqL0WiJBG578"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = URL(url).readText()
                val json = JSONObject(result)

                val route = json.getJSONArray("routes").getJSONObject(0)
                val leg = route.getJSONArray("legs").getJSONObject(0)
                val duration = leg.getJSONObject("duration").getString("text")
                val distance = leg.getJSONObject("distance").getString("text")
                val steps = parseRouteSteps(result)

                withContext(Dispatchers.Main) {
                    mMap.clear()

                    // Marcadores de origem e destino
                    mMap.addMarker(MarkerOptions().position(origin).title("Origem"))
                    mMap.addMarker(MarkerOptions().position(destination).title("Destino"))

                    // Desenha a linha
                    val polylineOptions = PolylineOptions()
                        .addAll(steps)
                        .width(10f)
                        .color(Color.BLUE)
                        .geodesic(true)
                    mMap.addPolyline(polylineOptions)

                    // Ajusta o zoom para mostrar tudo
                    val boundsBuilder = LatLngBounds.Builder()
                    steps.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    val padding = 120 // espaço nas bordas
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

                    // Atualiza info da rota
                    tvRouteInfo.text = "Distância: $distance | Tempo estimado: $duration"
                }
            } catch (e: Exception) {
                Log.e("RouteError", "Erro ao obter rota: ${e.message}")
            }
        }
    }


    private fun parseRouteSteps(jsonData: String): List<LatLng> {
        val json = JSONObject(jsonData)
        val stepsList = mutableListOf<LatLng>()

        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return stepsList

        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val steps = legs.getJSONObject(0).getJSONArray("steps")

        for (i in 0 until steps.length()) {
            val step = steps.getJSONObject(i)
            val polyline = step.getJSONObject("polyline").getString("points")
            stepsList.addAll(decodePolyline(polyline))
        }

        return stepsList
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap)
        }
    }
}
