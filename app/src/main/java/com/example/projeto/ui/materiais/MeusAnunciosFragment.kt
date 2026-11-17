package com.example.projeto.ui.materiais

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto.firebase.FirebaseRepository
import com.example.projeto.databinding.FragmentMeusAnunciosBinding
import kotlinx.coroutines.launch

class MeusAnunciosFragment : Fragment() {

    private var _binding: FragmentMeusAnunciosBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMeusAnunciosBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter()

        binding.recyclerMateriais.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }

        carregarMeusProdutos()
    }

    private fun carregarMeusProdutos() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val produtos = FirebaseRepository.getMyProducts()
                productAdapter.atualizarProdutos(produtos)
                binding.tvMeusMateriais.text = if (produtos.isEmpty())
                    "Você ainda não possui materiais cadastrados."
                else "${produtos.size} material(is) cadastrado(s)."

            } catch (e: Exception) {

                binding.tvMeusMateriais.text =
                    "Erro ao carregar os materiais."

                Toast.makeText(
                    requireContext(),
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}