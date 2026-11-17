package com.example.projeto.ui.apresentacao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.projeto.R

class ApresentacaoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_apresentacao, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        view.findViewById<View>(R.id.btnIntroComecar).setOnClickListener {
            requireContext().getSharedPreferences("rematch_intro", 0)
                .edit().putBoolean("intro_vista", true).apply()
            findNavController().navigate(R.id.LoginFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.ApresentacaoFragment, true).build())
        }
    }
}
