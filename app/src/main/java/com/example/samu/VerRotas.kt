package com.example.samu

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

            // Criar card para o resumo da rota
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

            val durationText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Duração: $duration"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                setTypeface(null, Typeface.BOLD)
            }
            summaryLayout.addView(durationText)

            val distanceText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
                text = "Distância: $distance"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            summaryLayout.addView(distanceText)

            // Adicionar os passos da rota
            val steps = legs.getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val travelMode = step.getString("travel_mode")
                val instruction = step.getString("html_instructions")
                    .replace("<[^>]*>".toRegex(), " ") // Remove HTML tags
                val stepDistance = step.getJSONObject("distance").getString("text")
                val stepDuration = step.getJSONObject("duration").getString("text")

                val stepCard = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 8, 16, 8)
                    }
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

                // Ícone para o tipo de passo
                val iconLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        48,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.TOP
                    setPadding(0, 4, 16, 0)
                }

                val stepIcon = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(32, 32)
                    setImageResource(getStepIcon(travelMode))
                    setColorFilter(ContextCompat.getColor(context, getStepIconColor(travelMode)))
                }
                iconLayout.addView(stepIcon)
                stepLayout.addView(iconLayout)

                // Conteúdo do passo
                val contentLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                }

                // Texto principal da instrução
                val instructionText = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = getFormattedInstruction(instruction, travelMode)
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }
                contentLayout.addView(instructionText)

                // Detalhes de distância e duração
                val detailsText = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 4
                    }
                    text = "$stepDistance · $stepDuration"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
                contentLayout.addView(detailsText)

                // Adicionar detalhes específicos para transporte público
                if (travelMode == "TRANSIT") {
                    val transitDetails = step.getJSONObject("transit_details")
                    val line = transitDetails.getJSONObject("line")
                    val vehicle = line.getJSONObject("vehicle")
                    val vehicleType = vehicle.getString("type")
                    val lineName = if (line.has("short_name")) line.getString("short_name") else line.getString("name")
                    val departureStop = transitDetails.getJSONObject("departure_stop").getString("name")
                    val arrivalStop = transitDetails.getJSONObject("arrival_stop").getString("name")
                    val headsign = transitDetails.getString("headsign")

                    val transitInfoLayout = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 12
                        }
                        orientation = LinearLayout.VERTICAL
                        setPadding(8, 8, 8, 8)
                        setBackgroundResource(R.drawable.transit_info_background)
                    }

                    val lineInfoText = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = "${getTransitTypeName(vehicleType)} $lineName em direção a $headsign"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        setTypeface(null, Typeface.BOLD)
                    }
                    transitInfoLayout.addView(lineInfoText)

                    val stopsInfoText = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 4
                        }
                        text = "De: $departureStop\nPara: $arrivalStop"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    }
                    transitInfoLayout.addView(stopsInfoText)

                    contentLayout.addView(transitInfoLayout)
                }

                stepLayout.addView(contentLayout)
                stepCard.addView(stepLayout)
                routesContainer.addView(stepCard)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying route", e)
            showError("Erro ao exibir rota: ${e.message}")
        }
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
                    "Andar até ${instruction.replace("Caminhar até ", "").replace("Caminhar para ", "")}"
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
