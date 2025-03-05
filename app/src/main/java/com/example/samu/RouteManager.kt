package com.example.samu

import android.util.Log
import com.example.samu.RouteRequest
import com.example.samu.RouteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteManager {

    private val api = RetrofitInstance.retrofit.create(OpenRouteServiceApi::class.java)

    // Função para obter a rota
    fun getRoute(startLatLng: List<Double>, endLatLng: List<Double>, callback: (RouteResponse?) -> Unit) {
        val requestBody = RouteRequest(
            coordinates = listOf(startLatLng, endLatLng)
        )

        val call = api.getRoute(requestBody)

        call.enqueue(object : Callback<RouteResponse> {
            override fun onResponse(call: Call<RouteResponse>, response: Response<RouteResponse>) {
                if (response.isSuccessful) {
                    // Quando a resposta for bem-sucedida
                    callback(response.body())
                } else {
                    Log.e("RouteManager", "Erro na resposta: ${response.errorBody()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<RouteResponse>, t: Throwable) {
                Log.e("RouteManager", "Erro na requisição: ${t.message}")
                callback(null)
            }
        })
    }
}
