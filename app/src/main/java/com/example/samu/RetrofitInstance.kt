package com.example.samu

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory.create

object RetrofitInstance {

    // Define a URL base para a API do OpenRouteService
    private const val BASE_URL = "https://api.openrouteservice.org/"

    // Configura o OkHttpClient para exibir os logs das requisições
    private val client = OkHttpClient.Builder().addInterceptor(
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    ).build()

    // Cria a instância do Retrofit
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())  // Certifique-se de que a conversão do Gson está aqui
        .build()
}
