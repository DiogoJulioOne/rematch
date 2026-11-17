package com.example.projeto.ui.mensagens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto.databinding.FragmentMensagensBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MensagensFragment : Fragment() {
    private var _binding: FragmentMensagensBinding? = null
    private val binding get() = _binding!!
    private var listener: ListenerRegistration? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMensagensBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.recyclerMensagens.layoutManager = LinearLayoutManager(requireContext())
        listener = FirebaseFirestore.getInstance().collection("conversas")
            .whereArrayContains("participantes", uid).addSnapshotListener { snapshot, _ ->
                if (_binding == null) return@addSnapshotListener
                val mensagens = snapshot?.documents?.map { doc ->
                    val outro = (doc.get("participantes") as? List<*>)?.filterIsInstance<String>()?.firstOrNull { it != uid }.orEmpty()
                    Mensagem(doc.getString("nomeOutro") ?: "Conversa", doc.getString("ultimaMensagem").orEmpty(), "", android.R.drawable.ic_menu_myplaces, doc.id, outro)
                }.orEmpty()
                binding.recyclerMensagens.adapter = MensagemAdapter(mensagens)
            }
    }
    override fun onDestroyView() { listener?.remove(); listener = null; _binding = null; super.onDestroyView() }
}
