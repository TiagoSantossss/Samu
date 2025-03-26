package com.example.samu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.samu.AccessTokenResponse
import com.example.samu.UberRetrofitInstance
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val clientId = "MuCWPi3C9yEIqL4OF6sl4XG6koNn0ByN"
    private val clientSecret = "u1UeRMPClZInWdlbUcH7ak9VgTdtlBtGk8dr7zdd"
    private val redirectUri = "com.example.samu://redirect"
    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Configurar login com Google
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("258972663841-s15dho0e2ivs552tqg1tfqikd1foee9c.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        startUberLogin()
    }

    private fun startUberLogin() {
        val authUrl = "https://login.uber.com/oauth/v2/authorize" +
                "?client_id=$clientId" +
                "&response_type=code" +
                "&redirect_uri=$redirectUri"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleUberRedirect(intent)
    }

    private fun handleUberRedirect(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "com.example.samu") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                Toast.makeText(this, "Código de autorização recebido: $code", Toast.LENGTH_LONG).show()
                Log.d("UberAPI", "Código de autorização: $code")
                getAccessToken(code)
            }
        }
    }

    private fun getAccessToken(authCode: String) {
        val call = UberRetrofitInstance.api.getAccessToken(clientId, clientSecret, authCode, redirectUri)

        call.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    accessToken = response.body()?.accessToken
                    Log.d("UberAPI", "Access Token recebido: $accessToken")
                    if (accessToken != null) {
                        fetchUberPricesAndRoutes()
                    }
                } else {
                    Log.e("UberAPI", "Erro: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                Log.e("UberAPI", "Falha na requisição: ${t.message}")
            }
        })
    }

    private fun fetchUberPricesAndRoutes() {
        if (accessToken == null) {
            Log.e("UberAPI", "Access Token não disponível")
            return
        }
        Log.d("UberAPI", "Pronto para consultar preços, tempos e rotas da Uber usando o token: $accessToken")
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        GOOGLE_SIGN_IN_REQUEST_CODE,
                        null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao iniciar login: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao iniciar login com Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Falha no login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao obter credenciais: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }
}