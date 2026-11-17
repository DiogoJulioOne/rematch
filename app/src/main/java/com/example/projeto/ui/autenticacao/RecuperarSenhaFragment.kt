package com.example.projeto.ui.autenticacao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projeto.databinding.FragmentRecuperarSenhaBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaFragment : Fragment() {
    private var _binding: FragmentRecuperarSenhaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecuperarSenhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvRecuperarVoltar.setOnClickListener { findNavController().navigateUp() }
        binding.btnRecuperarSenha.setOnClickListener {
            val email = binding.edtRecuperarEmail.text.toString().trim()
            if (email.isBlank()) {
                binding.edtRecuperarEmail.error = "Informe seu e-mail"
                return@setOnClickListener
            }
            binding.btnRecuperarSenha.isEnabled = false
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (_binding == null) return@addOnCompleteListener
                binding.btnRecuperarSenha.isEnabled = true
                Toast.makeText(requireContext(), if (task.isSuccessful) "Se o e-mail estiver cadastrado, confira sua caixa de entrada." else "Não foi possível solicitar o link. Tente novamente.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
