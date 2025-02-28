package com.example.samu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var oneTapClient: SignInClient? = null
    private var signInRequest: BeginSignInRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configurar ajustes para insets da tela
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Encontrar botões no layout
        val botaoRegisto = findViewById<Button>(R.id.botaoregisto)
        val botaoLogin = findViewById<Button>(R.id.botaologin)
        val botaoGoogle = findViewById<ImageView>(R.id.imageView2)

        // Adicionar ação para abrir a tela de Registo
        botaoRegisto.setOnClickListener { v: View? ->
            val intent = Intent(
                this,
                Registo::class.java
            )
            startActivity(intent)
        }

        // Adicionar ação para abrir a tela de Login
        botaoLogin.setOnClickListener { v: View? ->
            val intent = Intent(
                this,
                MapsActivity::class.java
            )
            startActivity(intent)
        }

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

        // Adicionar ação para login com Google
        botaoGoogle.setOnClickListener { v: View? -> signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        oneTapClient!!.beginSignIn(signInRequest!!)
            .addOnSuccessListener { result: BeginSignInResult ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        GOOGLE_SIGN_IN_REQUEST_CODE,
                        null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Erro ao iniciar login: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("GoogleSignIn", "Erro ao iniciar login com Google", e)
                Toast.makeText(this, "Erro: " + e.localizedMessage, Toast.LENGTH_LONG)
                    .show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        auth!!.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                            .addOnCompleteListener { task: Task<AuthResult?> ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Login com Google bem-sucedido!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this,
                                            MapsActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Falha no login: " + task.exception!!.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Erro ao obter credenciais: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Falha no login: Código de resultado inválido",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }
}