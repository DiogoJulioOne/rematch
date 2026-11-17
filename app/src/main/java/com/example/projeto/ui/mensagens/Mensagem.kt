package com.example.projeto.ui.mensagens

data class Mensagem(
    val nome: String,
    val ultimaMensagem: String,
    val horario: String,
    val foto: Int,
    val conversaId: String = "",
    val outroUsuarioId: String = ""
)
