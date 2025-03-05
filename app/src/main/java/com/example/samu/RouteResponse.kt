package com.example.samu

data class RouteResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: Geometry
)

data class Geometry(
    val coordinates: List<List<Double>> // Lista de coordenadas para desenhar a rota
)
