package com.example.projeto.ui.materiais

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.projeto.data.Product
import com.example.projeto.databinding.FragmentDetalhesMaterialBinding

class DetalhesMaterialFragment : Fragment() {

    private var _binding: FragmentDetalhesMaterialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDetalhesMaterialBinding.inflate(
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

        val produto = arguments?.getParcelable<Product>("produto")

        if (produto == null) {

            Toast.makeText(
                requireContext(),
                "Produto não encontrado.",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().popBackStack()
            return
        }

        mostrarProduto(produto)

        binding.btnVoltar.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnTenhoInteresse.setOnClickListener {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null || uid == produto.authorId) {
                Toast.makeText(requireContext(), "Entre com outra conta para demonstrar interesse", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val id = "${produto.id}_$uid"
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("interesses").document(id)
                .set(mapOf("materialId" to produto.id, "authorId" to produto.authorId, "interessadoId" to uid,
                    "status" to "pendente", "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
                .addOnSuccessListener { if (isAdded) Toast.makeText(requireContext(), "Interesse enviado", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { if (isAdded) Toast.makeText(requireContext(), "Não foi possível enviar interesse", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun mostrarProduto(produto: Product) {

        binding.tvTitulo.text = produto.title
        binding.tvModalidade.text = when (produto.modalidade) {
            "Doação" -> "♻  Doação"
            "Troca" -> "⇄  Troca"
            "Venda" -> "Venda"
            else -> "♻  Reaproveitamento"
        }
        binding.tvPreco.visibility = if (produto.modalidade == "Venda") View.VISIBLE else View.GONE
        binding.tvPreco.text = produto.preco?.let {
            "Preço: " + java.text.NumberFormat.getCurrencyInstance(
                java.util.Locale.forLanguageTag("pt-BR")).format(it)
        } ?: "Preço a combinar"
        binding.tvTrocaPor.visibility = if (produto.modalidade == "Troca" && !produto.trocaPor.isNullOrBlank()) View.VISIBLE else View.GONE
        binding.tvTrocaPor.text = "Aceita em troca: ${produto.trocaPor.orEmpty()}"
        binding.btnTenhoInteresse.text = when (produto.modalidade) {
            "Doação" -> "Solicitar doação"
            "Troca" -> "Propor troca"
            "Venda" -> "Tenho interesse"
            else -> "Solicitar"
        }

        binding.tvLocalizacao.text =
            if (produto.location.isNotBlank()) {
                "📍 ${produto.location}"
            } else {
                "📍 Localização não informada"
            }

        binding.tvCategoria.text =
            "🏷️ Categoria: ${produto.category}"

        binding.tvEstado.text =
            "📦 Estado: ${produto.state}"

        binding.tvQuantidade.text =
            "🔢 Quantidade: ${produto.quantity}"

        binding.tvDescricao.text =
            if (!produto.description.isNullOrBlank()) {
                produto.description
            } else {
                "Nenhuma descrição informada."
            }

        if (produto.photos.isNotEmpty()) {

            val photo = produto.photos.first()

            val photoUrl = photo.url

            Glide.with(this)
                .load(photoUrl)
                .into(binding.imgProduto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}