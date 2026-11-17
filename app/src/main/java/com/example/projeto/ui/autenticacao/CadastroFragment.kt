package com.example.projeto.ui.autenticacao

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projeto.firebase.FirebaseRepository
import com.example.projeto.databinding.FragmentCadastroBinding
import kotlinx.coroutines.launch
import com.example.projeto.R

class CadastroFragment : Fragment() {

    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCadastroBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnCadastrar.setOnClickListener {

            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmailCadastro.text.toString().trim()
            val senha = binding.edtSenhaCadastro.text.toString().trim()

            if (nome.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Digite seu nome",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (email.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Digite seu email",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (senha.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Digite sua senha",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (binding.edtConfirmarSenha.text.toString() != senha) {
                binding.edtConfirmarSenha.error = "As senhas não coincidem"
                return@setOnClickListener
            }
            if (!binding.checkTermosCadastro.isChecked) {
                Toast.makeText(requireContext(), "Aceite os termos para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnCadastrar.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    FirebaseRepository.register(nome, email, senha)
                    findNavController().navigate(R.id.action_CadastroFragment_to_InicioFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.localizedMessage ?: "Erro ao criar conta", Toast.LENGTH_LONG).show()
                } finally {
                    if (_binding != null) binding.btnCadastrar.isEnabled = true
                }
            }
        }

        binding.tvVoltarLogin.setOnClickListener {

            findNavController().navigate(
                R.id.action_CadastroFragment_to_LoginFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}