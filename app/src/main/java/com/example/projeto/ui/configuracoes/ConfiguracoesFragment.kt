package com.example.projeto.ui.configuracoes

import android.os.Bundle
import com.example.projeto.firebase.FirebaseRepository
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projeto.databinding.FragmentConfiguracoesBinding
import com.example.projeto.R

class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConfiguracoesBinding.inflate(
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

        binding.btnNotificacoes.setOnClickListener {
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_NotificacoesFragment
            )
        }

        binding.btnPrivacidade.setOnClickListener {
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_PrivacidadeFragment
            )
        }

        binding.btnAjuda.setOnClickListener {
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_AjudaFragment
            )
        }

        binding.btnTermos.setOnClickListener {
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_TermosFragment
            )
        }

        // Voltar para o Perfil
        binding.btnVoltarPerfil.setOnClickListener {
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_PerfilFragment
            )
        }

        // Sair da conta
        binding.btnSair.setOnClickListener {
            FirebaseRepository.logout()
            findNavController().navigate(
                R.id.action_ConfiguracoesFragment_to_LoginFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}