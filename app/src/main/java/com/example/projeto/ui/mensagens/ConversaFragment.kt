package com.example.projeto.ui.mensagens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import com.example.projeto.R

class ConversaFragment : Fragment() {
    private val mensagens = mutableListOf<ChatMensagem>()
    private var listener: ListenerRegistration? = null
    private lateinit var adapter: ChatMensagemAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_conversa, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerChat)
        val input = view.findViewById<EditText>(R.id.edtMensagem)
        val send = view.findViewById<Button>(R.id.btnEnviarMensagem)
        view.findViewById<TextView>(R.id.tvNomeChat).text = arguments?.getString("nomeUsuario") ?: "Conversa"
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val conversaId = arguments?.getString("conversaId").orEmpty()
        val db = FirebaseFirestore.getInstance()
        adapter = ChatMensagemAdapter(mensagens)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        if (uid == null || conversaId.isBlank()) {
            send.isEnabled = false
            Toast.makeText(requireContext(), "Selecione uma conversa válida", Toast.LENGTH_LONG).show()
            return
        }
        val conversa = db.collection("conversas").document(conversaId)
        listener = conversa.collection("mensagens").orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (view.parent == null && !isAdded) return@addSnapshotListener
                if (error != null) { if (isAdded) Toast.makeText(requireContext(), "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show(); return@addSnapshotListener }
                mensagens.clear()
                snapshot?.documents?.forEach { doc ->
                    mensagens.add(ChatMensagem(doc.getString("texto").orEmpty(), doc.getString("senderId") == uid))
                }
                adapter.notifyDataSetChanged()
                if (mensagens.isNotEmpty()) recycler.scrollToPosition(mensagens.lastIndex)
            }
        send.setOnClickListener {
            val texto = input.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            send.isEnabled = false
            conversa.get().addOnSuccessListener { doc ->
                val participantes = doc.get("participantes") as? List<*>
                if (participantes?.contains(uid) != true) {
                    send.isEnabled = true
                    Toast.makeText(requireContext(), "Você não participa desta conversa", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                conversa.collection("mensagens").add(mapOf("texto" to texto, "senderId" to uid, "createdAt" to FieldValue.serverTimestamp()))
                    .addOnSuccessListener { conversa.update("ultimaMensagem", texto, "updatedAt", FieldValue.serverTimestamp()); input.text.clear(); send.isEnabled = true }
                    .addOnFailureListener { send.isEnabled = true; if (isAdded) Toast.makeText(requireContext(), "Falha ao enviar", Toast.LENGTH_SHORT).show() }
            }.addOnFailureListener { send.isEnabled = true }
        }
    }
    override fun onDestroyView() { listener?.remove(); listener = null; super.onDestroyView() }
}
