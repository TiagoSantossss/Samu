package com.example.samu

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge

class VerRotas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_rotas)

        // Pegando os nomes enviados pela Intent
        val originName = intent.getStringExtra("origin_name") ?: "Origem desconhecida"
        val destName = intent.getStringExtra("dest_name") ?: "Destino desconhecido"

        // Referência aos elementos do layout
        val editTextOrigem = findViewById<EditText>(R.id.editOrigem)
        val editTextDestino = findViewById<EditText>(R.id.editDestino)
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)

        // Preenchendo os campos com os nomes dos locais
        editTextOrigem.setText(originName)
        editTextDestino.setText(destName)

        // Botão de voltar
        btnVoltar.setOnClickListener {
            finish()
        }
    }
}
