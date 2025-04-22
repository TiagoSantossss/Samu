package com.example.samu

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imageProfile: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var iconCamera: ImageView
    private lateinit var iconEditUsername: ImageView
    private lateinit var tvChangePassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Inicializar FirebaseAuth e Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referências da UI
        imageProfile = findViewById(R.id.imageProfile)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        iconCamera = findViewById(R.id.iconCamera)
        iconEditUsername = findViewById(R.id.iconEditUsername)
        tvChangePassword = findViewById(R.id.tvChangePassword)

        // Carregar dados do utilizador
        carregarDadosDoPerfil()

        // Ação para editar foto de perfil (câmera ou galeria)
        iconCamera.setOnClickListener {
            // Aqui deves colocar a lógica para abrir a galeria ou câmera
            abrirGaleriaOuCamera()
        }

        // Ação para editar o nome de utilizador
        iconEditUsername.setOnClickListener {
            // Lógica para editar o nome de utilizador (pode ser um Dialog ou outra activity)
            editarNomeDeUtilizador()
        }

        // Ação para mudar a palavra-passe
        tvChangePassword.setOnClickListener {
            // Lógica para alterar a palavra-passe
            alterarSenha()
        }
    }

    // Função para carregar os dados do perfil (Nome, Email, Foto de Perfil)
    private fun carregarDadosDoPerfil() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        val email = document.getString("email")

                        tvUsername.text = username ?: "Nome de utilizador"
                        tvEmail.text = email ?: "email@example.com"

                        // Aqui podemos carregar a foto de perfil se estiver salva no Firestore
                        // Se houver URL da imagem, use Glide ou Picasso para carregar
                        // Glide.with(this).load(urlDaImagem).into(imageProfile)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao carregar dados: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Função para abrir a galeria ou câmera
    private fun abrirGaleriaOuCamera() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)  // 100 é o código de requisição, pode ser alterado
    }

    // Função para editar o nome de utilizador
    private fun editarNomeDeUtilizador() {
        val currentUsername = tvUsername.text.toString()
        val editText = EditText(this).apply {
            setText(currentUsername)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Nome de Utilizador")
            .setMessage("Digite o novo nome de utilizador")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newUsername = editText.text.toString()
                if (newUsername.isNotBlank()) {
                    atualizarNomeDeUtilizador(newUsername)
                } else {
                    Toast.makeText(this, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }


    // Função para atualizar o nome de utilizador no Firestore
    private fun atualizarNomeDeUtilizador(newUsername: String) {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("users").document(userId).update("username", newUsername)
                .addOnSuccessListener {
                    tvUsername.text = newUsername
                    Toast.makeText(this, "Nome de utilizador atualizado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar nome: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Função para alterar a palavra-passe
    private fun alterarSenha() {
        val user = auth.currentUser
        if (user != null) {
            val email = user.email
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Instruções enviadas para o email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao enviar email: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    // Resultados da galeria ou câmera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                // Aqui você pode fazer o upload da imagem para o Firestore ou Firebase Storage
                // Para agora, estamos apenas mostrando na UI
                imageProfile.setImageURI(it)
            }
        }
    }
}
