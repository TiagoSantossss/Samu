package com.example.samu

import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import org.osmdroid.api.IMapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.util.GeoPoint

class MapsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchViewOrigin: SearchView
    private lateinit var searchViewDestination: SearchView
    private lateinit var routeManager: RouteManager
    private lateinit var startLatLng: GeoPoint
    private lateinit var endLatLng: GeoPoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)  // Configure o tile
        mapView.setBuiltInZoomControls(true)  // Habilitar controles de zoom
        mapView.setMultiTouchControls(true)  // Habilitar multi-touch para controle de zoom

        searchViewOrigin = findViewById(R.id.searchViewOrigin)
        searchViewDestination = findViewById(R.id.searchViewDestination)
        routeManager = RouteManager()

        // Configuração do mapa
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Botão de Traçar Rota
        findViewById<Button>(R.id.btnTraçarRota).setOnClickListener {
            if (::startLatLng.isInitialized && ::endLatLng.isInitialized) {
                // Traçar rota
                routeManager.getRoute(
                    listOf(startLatLng.longitude, startLatLng.latitude),
                    listOf(endLatLng.longitude, endLatLng.latitude)
                ) { routeResponse ->
                    if (routeResponse != null) {
                        // Adicionar a rota ao mapa
                        val polyline = Polyline()
                        for (coord in routeResponse.routes[0].geometry.coordinates) {
                            polyline.addPoint(GeoPoint(coord[1], coord[0]))
                        }
                        mapView.overlayManager.add(polyline)

                        // Configurar a posição do mapa
                        val controller: IMapController = mapView.controller
                        controller.setZoom(12)
                        controller.setCenter(startLatLng) // Centra o mapa na origem
                    } else {
                        Toast.makeText(this, "Erro ao obter a rota", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, defina ambos os locais de partida e destino", Toast.LENGTH_SHORT).show()
            }
        }

        // Inicializa as buscas
        setupSearchViews()
    }

    private fun setupSearchViews() {
        // Ação para a barra de pesquisa de origem
        searchViewOrigin.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // Definir local de origem
                    setLocation(query, true)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Ação para a barra de pesquisa de destino
        searchViewDestination.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // Definir local de destino
                    setLocation(query, false)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun setLocation(location: String, isStart: Boolean) {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = GeoPoint(address.latitude, address.longitude)
                if (isStart) {
                    startLatLng = latLng
                    val startMarker = Marker(mapView)
                    startMarker.position = latLng
                    startMarker.title = "Origem"
                    mapView.overlayManager.add(startMarker)
                } else {
                    endLatLng = latLng
                    val endMarker = Marker(mapView)
                    endMarker.position = latLng
                    endMarker.title = "Destino"
                    mapView.overlayManager.add(endMarker)
                }

                // Ajustar o centro do mapa para o local
                val controller: IMapController = mapView.controller
                controller.setZoom(12)
                controller.setCenter(latLng)
            } else {
                Toast.makeText(this, "Local não encontrado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao procurar o local", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Chama o método do OSMDroid para retomar o mapa
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Chama o método do OSMDroid para pausar o mapa
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()  // Chama o método para limpar o mapa
    }


}
