package com.example.samu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.samu.Mapa.MapsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configurar ajustes para insets da tela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Encontrar botões no layout
        val botaoRegisto = findViewById<Button>(R.id.botaoregisto)
        val botaoLogin = findViewById<Button>(R.id.botaologin)

        // Adicionar ação para abrir a tela de Registo
        botaoRegisto.setOnClickListener {
            val intent = Intent(this, Registo::class.java)
            startActivity(intent)
        }

        // Adicionar ação para abrir a tela de Login
        botaoLogin.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}
