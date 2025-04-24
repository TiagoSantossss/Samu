package com.example.samu

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge

class VerRotas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_rotas)

        // Aplicando padding para áreas do sistema (status bar, barra de navegação)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.linearLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pegando as coordenadas da intent
        val originLat = intent.getDoubleExtra("origin_lat", 0.0)
        val originLng = intent.getDoubleExtra("origin_lng", 0.0)
        val destLat = intent.getDoubleExtra("dest_lat", 0.0)
        val destLng = intent.getDoubleExtra("dest_lng", 0.0)

        // Referência aos elementos do layout
        val editTextOrigem = findViewById<EditText>(R.id.editTextText)
        val editTextDestino = findViewById<EditText>(R.id.editTextText2)
        val spinnerOrdem = findViewById<Spinner>(R.id.spinnerOrdem)
        val btnVoltar = findViewById<ImageButton>(R.id.imageButton)

        // Preenchendo os campos com os dados recebidos
        editTextOrigem.setText("Lat: $originLat, Lng: $originLng")
        editTextDestino.setText("Lat: $destLat, Lng: $destLng")

        // Botão de voltar
        btnVoltar.setOnClickListener {
            finish()
        }
    }
}
