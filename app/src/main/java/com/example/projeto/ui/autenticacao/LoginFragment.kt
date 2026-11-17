package com.example.projeto.ui.autenticacao

import android.os.Bundle
import android.widget.Toast
import com.example.projeto.firebase.FirebaseRepository
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projeto.databinding.FragmentLoginBinding
import com.example.projeto.R

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString()
            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(requireContext(), "Informe e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnEntrar.isEnabled = false
            com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (_binding == null) return@addOnCompleteListener
                    binding.btnEntrar.isEnabled = true
                    if (task.isSuccessful) {
                        findNavController().navigate(R.id.action_LoginFragment_to_InicioFragment)
                    } else {
                        Toast.makeText(requireContext(), task.exception?.localizedMessage ?: "Falha no login", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // BOTÃO CADASTRE-SE
        binding.tvEsqueciSenha.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RecuperarSenhaFragment)
        }

        binding.tvCadastro.setOnClickListener {

            findNavController().navigate(
                R.id.action_LoginFragment_to_CadastroFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}