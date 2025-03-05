package com.example.samu

data class RouteRequest(
    val coordinates: List<List<Double>> // Cada coordenada é uma lista de [longitude, latitude]
)
