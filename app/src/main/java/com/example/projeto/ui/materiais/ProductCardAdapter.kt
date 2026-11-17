package com.example.projeto.ui.materiais

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projeto.data.Product
import com.example.projeto.databinding.ItemProductCardBinding
import com.example.projeto.R

class ProductCardAdapter(
    private var produtos: List<Product>,
    private val onProdutoClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductCardAdapter.ProdutoViewHolder>() {

    inner class ProdutoViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: Product) {

            binding.tvTituloProduto.text = produto.title
            binding.tvModalidadeProduto.text = when (produto.modalidade) {
                "Doação" -> "♻ Doação"
                "Troca" -> "⇄ Troca"
                "Venda" -> {
                    val preco = produto.preco?.let { java.text.NumberFormat.getCurrencyInstance(
                        java.util.Locale.forLanguageTag("pt-BR")).format(it) } ?: "Preço a combinar"
                    "Venda • $preco"
                }
                else -> "♻ Reaproveitamento"
            }

            binding.tvLocalizacaoProduto.text =
                if (produto.location.isNotBlank()) {
                    "📍 ${produto.location}"
                } else {
                    "📍 Localização não informada"
                }

            if (produto.photos.isNotEmpty()) {

                val photo = produto.photos.first()

                val photoUrl = photo.url

                Glide.with(binding.imgProduto.context)
                    .load(photoUrl)
                    .into(binding.imgProduto)

            } else {

                binding.imgProduto.setImageResource(
                    R.drawable.eco_sprout_box
                )
            }

            binding.btnVerProduto.setOnClickListener {
                onProdutoClick(produto)
            }

            binding.root.setOnClickListener {
                onProdutoClick(produto)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProdutoViewHolder {

        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProdutoViewHolder,
        position: Int
    ) {
        holder.bind(produtos[position])
    }

    override fun getItemCount(): Int {
        return produtos.size
    }

    fun atualizarProdutos(novosProdutos: List<Product>) {
        produtos = novosProdutos
        notifyDataSetChanged()
    }
}