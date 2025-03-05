package com.example.samu

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint

class MapsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchViewOrigin: SearchView
    private lateinit var searchViewDestination: SearchView
    private lateinit var btnTraçarRota: Button

    private var originPoint: GeoPoint? = null
    private var destinationPoint: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a configuração global do OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("OSMDroid", MODE_PRIVATE))

        setContentView(R.layout.activity_maps)

        // Inicializa os componentes de UI
        mapView = findViewById(R.id.mapView)
        searchViewOrigin = findViewById(R.id.searchViewOrigin)
        searchViewDestination = findViewById(R.id.searchViewDestination)
        btnTraçarRota = findViewById(R.id.btnTraçarRota)

        // Configura o MapView
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController: IMapController = mapView.controller
        mapController.setZoom(15)

        // Define ponto central inicial (latitude e longitude)
        val startPoint = GeoPoint(48.8583, 2.2944)  // Ponto em Paris (Eiffel Tower)
        mapController.setCenter(startPoint)

        // Ação de pesquisa para a origem
        setupSearch(searchViewOrigin, true)

        // Ação de pesquisa para o destino
        setupSearch(searchViewDestination, false)

        // Configuração do botão para traçar a rota
        btnTraçarRota.setOnClickListener {
            if (originPoint != null && destinationPoint != null) {
                traceRoute(originPoint!!, destinationPoint!!)
            } else {
                Toast.makeText(this, "Selecione ambos os locais de origem e destino.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Configuração da barra de pesquisa
    private fun setupSearch(searchView: SearchView, isOrigin: Boolean) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchLocation(query, isOrigin)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // Método para buscar a localização do usuário
    private fun searchLocation(location: String, isOrigin: Boolean) {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = GeoPoint(address.latitude, address.longitude)

                if (isOrigin) {
                    originPoint = latLng
                    addMarker(latLng, "Origem")
                } else {
                    destinationPoint = latLng
                    addMarker(latLng, "Destino")
                }
            } else {
                Toast.makeText(this, "Local não encontrado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Erro ao procurar a localização", e)
            Toast.makeText(this, "Erro ao procurar a localização", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para adicionar marcador no mapa
    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.title = title
        mapView.overlays.add(marker)
        mapView.controller.setCenter(point)
    }

    // Método para traçar a rota entre origem e destino
    private fun traceRoute(origin: GeoPoint, destination: GeoPoint) {
        // Aqui você pode usar a API de rotas ou algoritmos como o A* para traçar a rota entre os pontos.
        // Neste exemplo, vamos apenas adicionar um marcador para a rota.

        // Adicionando um marcador entre os dois pontos (apenas exemplo, substitua por cálculo real de rota)
        val routeMarker = Marker(mapView)
        routeMarker.position = destination
        routeMarker.title = "Rota traçada"
        mapView.overlays.add(routeMarker)

        // Zoom para a rota traçada
        mapView.controller.setZoom(12)
        mapView.controller.setCenter(origin)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Essencial para o ciclo de vida do MapView
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Essencial para o ciclo de vida do MapView
    }

    // Método correto para gerenciar a destruição do MapView
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()  // Chama o método correto para liberar o MapView no ciclo de vida
    }

}

