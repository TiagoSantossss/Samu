package com.example.samu

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface UberAuthService {
    @FormUrlEncoded
    @POST("oauth/v2/token")
    fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("redirect_uri") redirectUri: String = "https://localhost",
        @Field("scope") scope: String = "profile history rides.request"
    ): Call<AccessTokenResponse>
}

