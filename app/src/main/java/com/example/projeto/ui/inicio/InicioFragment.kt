package com.example.projeto.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projeto.databinding.FragmentInicioBinding
import com.example.projeto.firebase.FirebaseRepository
import com.example.projeto.data.Product
import kotlinx.coroutines.launch
import com.example.projeto.R
import com.example.projeto.ui.materiais.ProductCardAdapter

class InicioFragment : Fragment() {
    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val adapter = ProductCardAdapter(emptyList()) { material: Product ->
            findNavController().navigate(R.id.DetalhesMaterialFragment, Bundle().apply {
                putParcelable("produto", material)
            })
        }
        binding.homeMateriais.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.homeMateriais.adapter = adapter
        binding.homeMateriais.isNestedScrollingEnabled = false

        listOf(
            binding.catPlastico to "Plástico",
            binding.catPapel to "Papel",
            binding.catVidro to "Vidro",
            binding.catMetal to "Metal"
        ).forEach { (view, categoria) ->
            view.setOnClickListener {
                findNavController().navigate(R.id.ExplorarMateriaisFragment, Bundle().apply {
                    putString("categoria", categoria)
                })
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { FirebaseRepository.getProfile() }.onSuccess { user ->
                if (_binding != null) {
                    val nome = user.name?.trim()?.substringBefore(" ").orEmpty()
                    binding.tvHomeSaudacao.text = if (nome.isEmpty()) "Olá! 👋" else "Olá, $nome! 👋"
                }
            }
            val produtos = runCatching { FirebaseRepository.getProducts() }.getOrDefault(emptyList())
            if (_binding != null) {
                val disponiveis = produtos.take(12)
                adapter.atualizarProdutos(disponiveis)
                binding.homeLoading.visibility = View.GONE
                binding.homeVazio.visibility = if (disponiveis.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
