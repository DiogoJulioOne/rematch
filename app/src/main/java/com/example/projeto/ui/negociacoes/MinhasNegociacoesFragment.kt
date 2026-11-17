package com.example.projeto.ui.negociacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projeto.databinding.FragmentMinhasNegociacoesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MinhasNegociacoesFragment : Fragment() {
    private var _binding: FragmentMinhasNegociacoesBinding? = null
    private val binding get() = _binding!!
    private var listener: ListenerRegistration? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMinhasNegociacoesBinding.inflate(inflater, container, false)
        return _binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        listener = FirebaseFirestore.getInstance().collection("interesses").whereEqualTo("interessadoId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (_binding == null) return@addSnapshotListener
                val text = if (snapshot?.isEmpty != false) "Nenhum interesse enviado ainda" else "Interesses enviados: ${snapshot.size()}"
                binding.tvMatches.text = text
            }
    }
    override fun onDestroyView() { listener?.remove(); listener = null; _binding = null; super.onDestroyView() }
}
