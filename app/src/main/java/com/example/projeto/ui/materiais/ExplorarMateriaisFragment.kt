package com.example.projeto.ui.materiais

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projeto.data.Product
import com.example.projeto.databinding.FragmentExplorarMateriaisBinding
import com.example.projeto.firebase.FirebaseRepository
import kotlinx.coroutines.launch
import com.example.projeto.R

class ExplorarMateriaisFragment : Fragment() {
    private var _binding: FragmentExplorarMateriaisBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductCardAdapter
    private var todosProdutos = emptyList<Product>()
    private var modalidadeFiltro: String? = null
    private var categoriaFiltro: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentExplorarMateriaisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        categoriaFiltro = arguments?.getString("categoria")
        adapter = ProductCardAdapter(emptyList()) { produto ->
            findNavController().navigate(R.id.action_ExplorarMateriaisFragment_to_DetalhesMaterialFragment,
                Bundle().apply { putParcelable("produto", produto) })
        }
        binding.recyclerProdutos.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerProdutos.adapter = adapter
        binding.btnBuscar.setOnClickListener { aplicarFiltros() }
        val chips = listOf(
            binding.chipTodos to null,
            binding.chipDoacao to "Doação",
            binding.chipTroca to "Troca",
            binding.chipVenda to "Venda"
        )
        chips.forEach { (chip, valor) ->
            chip.setOnClickListener {
                modalidadeFiltro = valor
                chips.forEach { (bt, v) ->
                    bt.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                        requireContext(), if (v == valor) R.color.rm_primary else R.color.rm_tile)
                    bt.setTextColor(androidx.core.content.ContextCompat.getColor(
                        requireContext(), if (v == valor) R.color.rm_on_primary else R.color.rm_primary_dark))
                }
                aplicarFiltros()
            }
        }
        binding.btnFiltroCategorias.setOnClickListener {
            val categorias = arrayOf("Todas", "Plástico", "Papel", "Vidro", "Metal", "Madeira", "Tecido", "Eletrônicos", "Móveis", "Roupas", "Outros")
            AlertDialog.Builder(requireContext())
                .setTitle("Categoria dos materiais")
                .setItems(categorias) { _, indice ->
                    categoriaFiltro = categorias[indice].takeUnless { it == "Todas" }
                    binding.btnFiltroCategorias.text = categoriaFiltro?.let { "☷ $it" } ?: "☷ Categorias"
                    aplicarFiltros()
                }
                .show()
        }
        if (!categoriaFiltro.isNullOrEmpty()) binding.btnFiltroCategorias.text = "☷ $categoriaFiltro"
        carregarProdutos()
    }

    private fun carregarProdutos() {
        binding.exploreLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                todosProdutos = FirebaseRepository.getProducts().filter { it.authorId != uid }
                aplicarFiltros()
            } catch (e: Exception) {
                if (_binding != null) Toast.makeText(requireContext(),
                    "Não foi possível carregar os materiais", Toast.LENGTH_LONG).show()
            } finally {
                if (_binding != null) binding.exploreLoading.visibility = View.GONE
            }
        }
    }

    private fun aplicarFiltros() {
        if (_binding == null || !::adapter.isInitialized) return
        val busca = binding.edtBusca.text.toString().trim()
        val filtrados = todosProdutos.filter { material ->
            (busca.isBlank() || material.title.contains(busca, true) || material.description?.contains(busca, true) == true) &&
            (modalidadeFiltro == null || material.modalidade == modalidadeFiltro) &&
            (categoriaFiltro == null || material.category.equals(categoriaFiltro, true) ||
                material.category.lowercase() == categoriaFiltro.orEmpty().lowercase()
                    .replace("á", "a").replace("ô", "o").replace("ê", "e"))
        }
        adapter.atualizarProdutos(filtrados)
        binding.exploreVazio.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
