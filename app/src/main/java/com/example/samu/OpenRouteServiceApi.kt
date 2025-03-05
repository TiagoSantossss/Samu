package com.example.samu

import com.example.samu.RouteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenRouteServiceApi {

    // Definir o endpoint para rota
    @Headers("Authorization: 5b3ce3597851110001cf6248fdeab8f185364c8585bf4ef16e90a89e") // Substitua com a sua chave de API
    @POST("v2/directions/driving-car") // Ou qualquer outro tipo de transporte que você queira
    fun getRoute(@Body requestBody: RouteRequest): Call<RouteResponse>
}
