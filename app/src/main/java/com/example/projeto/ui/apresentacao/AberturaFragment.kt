package com.example.projeto.ui.apresentacao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.projeto.R

class AberturaFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_abertura, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        view.postDelayed({
            if (!isAdded) return@postDelayed
            val nav = findNavController()
            if (nav.currentDestination?.id != R.id.AberturaFragment) return@postDelayed
            val prefs = requireContext().getSharedPreferences("rematch_intro", 0)
            val next = when {
                !prefs.getBoolean("intro_vista", false) -> R.id.ApresentacaoFragment
                FirebaseAuth.getInstance().currentUser != null -> R.id.InicioFragment
                else -> R.id.LoginFragment
            }
            nav.navigate(next, null,
                NavOptions.Builder().setPopUpTo(R.id.AberturaFragment, true).build())
        }, 700)
    }
}
