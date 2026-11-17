package com.example.projeto.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projeto.firebase.FirebaseRepository
import com.example.projeto.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch
import com.example.projeto.R

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarPerfil()

        binding.btnConfiguracoes.setOnClickListener {
            findNavController().navigate(R.id.action_PerfilFragment_to_ConfiguracoesFragment)
        }
        binding.btnEditarPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_PerfilFragment_to_EditarPerfilFragment)
        }
        binding.btnMeusMateriais.setOnClickListener {
            findNavController().navigate(R.id.action_PerfilFragment_to_MeusAnunciosFragment)
        }
        binding.btnMatches.setOnClickListener {
            findNavController().navigate(R.id.action_PerfilFragment_to_MinhasNegociacoesFragment)
        }
    }

    private fun carregarPerfil() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = FirebaseRepository.getProfile()
                if (_binding == null) return@launch

                val nome = user.name?.trim().orEmpty().ifEmpty { "Usuário" }
                binding.tvNomeUsuario.text = nome
                binding.tvAvatarIniciais.text = nome.first().uppercaseChar().toString()
                if (!user.photoUrl.isNullOrBlank()) {
                    binding.tvAvatarIniciais.visibility = View.GONE
                    binding.imgPerfilFoto.visibility = View.VISIBLE
                    com.bumptech.glide.Glide.with(binding.imgPerfilFoto)
                        .load(user.photoUrl)
                        .circleCrop()
                        .into(binding.imgPerfilFoto)
                }
                runCatching { FirebaseRepository.getMyProducts() }.onSuccess { materiais ->
                    if (_binding != null) {
                        binding.tvTotalAnuncios.text = materiais.size.toString()
                        binding.tvTotalTrocas.text = materiais.count { it.modalidade == "Troca" }.toString()
                        binding.tvTotalDoacoes.text = materiais.count { it.modalidade == "Doação" }.toString()
                    }
                }
                binding.tvLocalizacao.text = if (!user.locale.isNullOrBlank())
                    "📍 ${user.locale}" else "📍 Localização não informada"
                binding.tvDescricao.text = if (!user.bio.isNullOrBlank())
                    user.bio else "Conte um pouco sobre você..."
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar perfil: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
