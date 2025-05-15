package com.example.samu

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class VerRotas : AppCompatActivity() {

    private val apiKey = "AIzaSyDdxlVIO3mbaFrf9g3PxnJWqL0WiJBG578"
    private lateinit var transportesTab: TextView
    private lateinit var aPeTab: TextView
    private lateinit var bicicletaTab: TextView
    private lateinit var privadosTab: TextView
    private lateinit var privadosContainer: ScrollView


    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var routesContainer: LinearLayout

    private lateinit var originText: TextView
    private lateinit var destinoText: TextView

    private val TAG = "VerRotas"
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var originLat = 0.0
    private var originLng = 0.0
    private var destLat = 0.0
    private var destLng = 0.0
    private var originAddress = ""
    private var destAddress = ""
    private var currentMode = "transit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_rotas)

        setupViews()
        getIntentData()
        setupTabListeners()

        if (originLat != 0.0 && originLng != 0.0 && destLat != 0.0 && destLng != 0.0) {
            originText.text = originAddress.ifEmpty { "Origem" }
            destinoText.text = destAddress.ifEmpty { "Destino" }
            fetchRoutes(originLat, originLng, destLat, destLng, currentMode)
        } else {
            showError("Coordenadas inválidas")
        }

        val originName = intent.getStringExtra("origin_name") ?: "Origem desconhecida"
        val destName = intent.getStringExtra("dest_name") ?: "Destino desconhecido"

        // Exibir os nomes nos TextViews
        originText.text = originName
        destinoText.text = destName

        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }


    }

    private fun setupViews() {
        transportesTab = findViewById(R.id.transportes_tab)
        aPeTab = findViewById(R.id.a_pe_tab)

        privadosTab = findViewById(R.id.privados_tab)
        privadosContainer = findViewById(R.id.privados_container)

        val uberButton = findViewById<Button>(R.id.uber_button)
        uberButton.setOnClickListener {
            val uri = Uri.parse(
                "https://m.uber.com/ul/?" +
                        "action=setPickup" +
                        "&pickup=my_location" +
                        "&dropoff[latitude]=$destLat" +
                        "&dropoff[longitude]=$destLng" +
                        "&dropoff[nickname]=Destino"
            )

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        progressBar = findViewById(R.id.progress_bar)
        errorLayout = findViewById(R.id.error_layout)
        errorText = findViewById(R.id.error_text)
        retryButton = findViewById(R.id.retry_button)
        routesContainer = findViewById(R.id.routes_container)

        originText = findViewById(R.id.origin_text)
        destinoText = findViewById(R.id.destino_text)

        retryButton.setOnClickListener {
            if (originLat != 0.0 && originLng != 0.0 && destLat != 0.0 && destLng != 0.0) {
                fetchRoutes(originLat, originLng, destLat, destLng, currentMode)
            }
        }
    }

    private fun getIntentData() {
        originLat = intent.getDoubleExtra("origin_lat", 0.0)
        originLng = intent.getDoubleExtra("origin_lng", 0.0)
        destLat = intent.getDoubleExtra("dest_lat", 0.0)
        destLng = intent.getDoubleExtra("dest_lng", 0.0)
        originAddress = intent.getStringExtra("origin_address") ?: ""
        destAddress = intent.getStringExtra("dest_address") ?: ""
    }

    private fun setupTabListeners() {
        transportesTab.setOnClickListener {
            selectTab("transit")
        }

        aPeTab.setOnClickListener {
            selectTab("walking")
        }

        privadosTab.setOnClickListener {
            selectTab("privados")
        }
    }

    private fun selectTab(mode: String) {
        currentMode = mode

        transportesTab.setTextColor(ContextCompat.getColor(this, R.color.tab_inactive))
        aPeTab.setTextColor(ContextCompat.getColor(this, R.color.tab_inactive))
        privadosTab.setTextColor(ContextCompat.getColor(this, R.color.tab_inactive))

        findViewById<View>(R.id.transportes_indicator).visibility = View.INVISIBLE
        findViewById<View>(R.id.a_pe_indicator).visibility = View.INVISIBLE
        findViewById<View>(R.id.privados_indicator).visibility = View.INVISIBLE

        // Esconder todos os containers
        routesContainer.visibility = View.GONE
        privadosContainer.visibility = View.GONE
        errorLayout.visibility = View.GONE
        progressBar.visibility = View.GONE

        when (mode) {
            "transit" -> {
                transportesTab.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                findViewById<View>(R.id.transportes_indicator).visibility = View.VISIBLE
                fetchRoutes(originLat, originLng, destLat, destLng, "transit")
            }
            "walking" -> {
                aPeTab.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                findViewById<View>(R.id.a_pe_indicator).visibility = View.VISIBLE
                fetchRoutes(originLat, originLng, destLat, destLng, "walking")
            }
            "privados" -> {
                privadosTab.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                findViewById<View>(R.id.privados_indicator).visibility = View.VISIBLE
                privadosContainer.visibility = View.VISIBLE
            }
        }
    }


    private fun fetchRoutes(originLat: Double, originLng: Double, destLat: Double, destLng: Double, mode: String) {
        showLoading()
        routesContainer.removeAllViews()

        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val origin = URLEncoder.encode("$originLat,$originLng", "UTF-8")
                    val destination = URLEncoder.encode("$destLat,$destLng", "UTF-8")

                    val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                            "origin=$origin" +
                            "&destination=$destination" +
                            "&mode=$mode" +
                            "&alternatives=false" +
                            "&language=pt-PT" +
                            "&transit_mode=bus|subway|train|tram" +  // Especificar modos de transporte
                            "&transit_routing_preference=fewer_transfers" +  // Preferência por menos transferências
                            "&key=$apiKey"

                    URL(url).readText()
                }

                val jsonResponse = JSONObject(result)
                val status = jsonResponse.getString("status")

                if (status == "OK") {
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        displayRoute(route, mode)
                        showRoutes()
                    } else {
                        showError("Nenhuma rota encontrada")
                    }
                } else {
                    showError("Erro ao procurar rotas: $status")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching routes", e)
                showError("Erro ao procurar rotas: For input string: \"#ebbc14\" under radix 16")
            }
        }
    }

    private fun displayRoute(route: JSONObject, mode: String) {
        try {
            val legs = route.getJSONArray("legs").getJSONObject(0)
            val distance = legs.getJSONObject("distance").getString("text")
            val duration = legs.getJSONObject("duration").getString("text")
            val steps = legs.getJSONArray("steps")

            var custoTotal = 0.0

            // Criação do cartão de resumo
            val summaryCard = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 8)
                }
                radius = 8f
                cardElevation = 4f
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            val summaryLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            // Duração
            val durationText = TextView(this).apply {
                text = "Duração: $duration"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
            summaryLayout.addView(durationText)

            // Distância
            val distanceText = TextView(this).apply {
                text = "Distância: $distance"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            }
            summaryLayout.addView(distanceText)

            // Verifica se há preço direto da API
            val fareFromAPI = if (route.has("fare")) route.getJSONObject("fare").optString("text", null) else null

            if (!fareFromAPI.isNullOrBlank()) {
                val fareTextView = TextView(this).apply {
                    text = "Custo estimado: $fareFromAPI"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 4 }
                }
                summaryLayout.addView(fareTextView)
            } else {
                // Calcular custo com base nos meios de transporte
                for (i in 0 until steps.length()) {
                    val step = steps.getJSONObject(i)
                    if (step.getString("travel_mode") == "TRANSIT") {
                        val transitDetails = step.getJSONObject("transit_details")
                        val line = transitDetails.getJSONObject("line")
                        val vehicle = line.getJSONObject("vehicle")
                        val vehicleType = vehicle.getString("type")
                        val agencyName = if (line.has("agencies")) {
                            line.getJSONArray("agencies").optJSONObject(0)?.optString("name") ?: ""
                        } else ""

                        val stepDistance = step.getJSONObject("distance").getString("text")
                            .replace(" km", "")
                            .replace(",", ".")
                            .toDoubleOrNull() ?: continue

                        when {
                            agencyName.contains("STCP", ignoreCase = true) -> {
                                custoTotal += 2.50
                            }
                            vehicleType.contains("TRAIN", ignoreCase = true) ||
                                    vehicleType.contains("RAIL", ignoreCase = true) -> {
                                custoTotal += calcularPrecoSimples2(stepDistance)
                            }
                            vehicleType.contains("BUS", ignoreCase = true) -> {
                                custoTotal += calcularPrecoSimples(stepDistance)
                            }
                        }
                    }
                }

                if (custoTotal > 0) {
                    val totalCostText = TextView(this).apply {
                        text = "Custo estimado (aproximado): €%.2f".format(custoTotal)
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 4 }
                    }
                    summaryLayout.addView(totalCostText)
                }
            }

            summaryCard.addView(summaryLayout)
            routesContainer.addView(summaryCard)

            // Passos da rota
            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val travelMode = step.getString("travel_mode")
                val instruction = step.getString("html_instructions").replace("<[^>]*>".toRegex(), " ")
                val stepDistance = step.getJSONObject("distance").getString("text")
                val stepDuration = step.getJSONObject("duration").getString("text")

                val stepCard = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 8, 16, 8) }
                    radius = 8f
                    cardElevation = 2f
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                val stepLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 16, 16, 16)
                }

                val iconLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(48, LinearLayout.LayoutParams.MATCH_PARENT)
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    setPadding(0, 4, 16, 0)
                }

                val stepIcon = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(32, 32)
                    setImageResource(getStepIcon(travelMode))
                    setColorFilter(ContextCompat.getColor(context, getStepIconColor(travelMode)))
                }

                iconLayout.addView(stepIcon)
                stepLayout.addView(iconLayout)

                val contentLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                }

                val instructionText = TextView(this).apply {
                    text = getFormattedInstruction(instruction, travelMode)
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }

                val detailsText = TextView(this).apply {
                    text = "$stepDistance · $stepDuration"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 4 }
                }

                contentLayout.addView(instructionText)
                contentLayout.addView(detailsText)

                // Adicionar detalhes específicos para transporte público
                if (travelMode == "TRANSIT") {
                    val transitDetails = step.getJSONObject("transit_details")
                    val line = transitDetails.getJSONObject("line")
                    val vehicle = line.getJSONObject("vehicle")
                    val vehicleType = vehicle.getString("type")

                    // Obter informações detalhadas da linha
                    val lineName = if (line.has("short_name") && !line.isNull("short_name")) {
                        line.getString("short_name")
                    } else if (line.has("name") && !line.isNull("name")) {
                        line.getString("name")
                    } else {
                        ""
                    }

                    // Obter nome da agência
                    val agencyName = if (line.has("agencies")) {
                        line.getJSONArray("agencies").optJSONObject(0)?.optString("name") ?: ""
                    } else ""

                    // Obter estações de partida e chegada
                    val departureStop = transitDetails.getJSONObject("departure_stop").getString("name")
                    val arrivalStop = transitDetails.getJSONObject("arrival_stop").getString("name")
                    val numStops = transitDetails.getInt("num_stops")

                    // Criar layout para informações detalhadas do transporte
                    val transitInfoLayout = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 8 }
                        orientation = LinearLayout.VERTICAL
                        setPadding(8, 8, 8, 8)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.background_light))
                    }

                    // Informações da linha e tipo de transporte
                    val lineInfoText = TextView(this).apply {
                        val transportType = getTransitTypeName(vehicleType)
                        text = if (lineName.isNotEmpty()) {
                            "$transportType $lineName${if (agencyName.isNotEmpty()) " • $agencyName" else ""}"
                        } else {
                            "$transportType${if (agencyName.isNotEmpty()) " • $agencyName" else ""}"
                        }
                        textSize = 14f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    }
                    transitInfoLayout.addView(lineInfoText)

                    // Informações de paradas
                    val stopsInfoText = TextView(this).apply {
                        text = "De $departureStop até $arrivalStop • $numStops ${if (numStops == 1) "paragem" else "paragens"}"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 4 }
                    }
                    transitInfoLayout.addView(stopsInfoText)

                    // Adicionar horários se disponíveis
                    if (transitDetails.has("departure_time") && transitDetails.has("arrival_time")) {
                        val departureTime = transitDetails.getJSONObject("departure_time").getString("text")
                        val arrivalTime = transitDetails.getJSONObject("arrival_time").getString("text")

                        val timeInfoText = TextView(this).apply {
                            text = "Horário: $departureTime - $arrivalTime"
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { topMargin = 4 }
                        }
                        transitInfoLayout.addView(timeInfoText)
                    }

                    // Adicionar frequência se disponível
                    if (transitDetails.has("headway")) {
                        val headwayMinutes = transitDetails.getInt("headway") / 60
                        val frequencyText = TextView(this).apply {
                            text = "Frequência: a cada $headwayMinutes minutos"
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { topMargin = 4 }
                        }
                        transitInfoLayout.addView(frequencyText)
                    }

                    contentLayout.addView(transitInfoLayout)
                }

                stepLayout.addView(contentLayout)
                stepCard.addView(stepLayout)
                routesContainer.addView(stepCard)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exibir rota", e)
            showError("Erro ao exibir rota: ${e.message}")
        }
    }


    private fun calcularPrecoSimples(distanciaKm: Double): Double {
        val precoBase = 1.20  // valor base
        val precoPorKm = 0.07 // custo por km
        return precoBase + (distanciaKm * precoPorKm)
    }

    private fun calcularPrecoSimples2(distanciaKm: Double): Double {
        val precoBase = 1
        val precoPorKm = 0.05
        return precoBase + (distanciaKm * precoPorKm)
    }

    private fun getStepIcon(travelMode: String): Int {
        return when (travelMode) {
            "WALKING" -> R.drawable.ic_walking
            "TRANSIT" -> R.drawable.ic_transit
            "BICYCLING" -> R.drawable.ic_bicycling
            else -> R.drawable.ic_walking
        }
    }

    private fun getStepIconColor(travelMode: String): Int {
        return when (travelMode) {
            "WALKING" -> R.color.walking_color
            "TRANSIT" -> R.color.transit_color
            "BICYCLING" -> R.color.bicycling_color
            else -> R.color.walking_color
        }
    }

    private fun getFormattedInstruction(instruction: String, travelMode: String): String {
        return when (travelMode) {
            "WALKING" -> {
                if (instruction.contains("Destino")) {
                    instruction
                } else {
                    "${instruction.replace("Caminhar até ", "").replace("Caminhar para ", "")}"
                }
            }
            "TRANSIT" -> instruction
            else -> instruction
        }
    }

    private fun getTransitTypeName(vehicleType: String): String {
        return when (vehicleType.lowercase(Locale.getDefault())) {
            "subway" -> "Metro"
            "bus" -> "Autocarro"
            "tram" -> "Elétrico"
            "train" -> "Comboio"
            "ferry" -> "Barco"
            else -> vehicleType
        }
    }

    private fun showRoutes() {
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.GONE

        if (currentMode == "privados") {
            privadosContainer.visibility = View.VISIBLE
        } else {
            routesContainer.visibility = View.VISIBLE
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        routesContainer.visibility = View.GONE
        privadosContainer.visibility = View.GONE
    }


    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        routesContainer.visibility = View.GONE
        errorText.text = message
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

}
