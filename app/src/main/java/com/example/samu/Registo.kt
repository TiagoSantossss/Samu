package com.example.samu

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Registo : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registo)

        // Inicializar FirebaseAuth e Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val emailEditText = findViewById<TextInputEditText>(R.id.preencheremail)
        val passwordEditText = findViewById<TextInputEditText>(R.id.preencherpassregisto)
        val registerButton = findViewById<Button>(R.id.botaoregisto)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()


            // Validação simples
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registrarUsuario(email, password)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun registrarUsuario(email: String, password: String) {
        // Criar um novo usuário com email e senha
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro bem-sucedido, salvar os dados do usuário
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val email = it.email

                        // Salvar no Firestore
                        salvarDadosNoFirestore(userId, email)
                    }
                } else {
                    // Tratar erro
                    Toast.makeText(this, "Erro ao registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun salvarDadosNoFirestore(userId: String, email: String?) {
        // Criar um objeto de dados do usuário
        val userMap = hashMapOf(
            "userId" to userId,
            "email" to email
        )

        // Salvar no Firestore
        db.collection("users")
            .document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show()
                // Redirecionar para outra Activity ou fazer algo após o sucesso
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
