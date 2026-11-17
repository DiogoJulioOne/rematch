package com.example.projeto

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavOptions
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.projeto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment_content_main
            ) as NavHostFragment

        val navController =
            navHostFragment.navController

        appBarConfiguration =
            AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )

        // Atalhos na barra inferior: destinos reais do aplicativo.
        val destinos = mapOf(
            R.id.navHome to R.id.InicioFragment,
            R.id.navExplorar to R.id.ExplorarMateriaisFragment,
            R.id.navAnunciar to R.id.PublicarMaterialFragment,
            R.id.navMensagens to R.id.MensagensFragment,
            R.id.navPerfil to R.id.PerfilFragment
        )
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val destino = destinos[item.itemId] ?: return@setOnItemSelectedListener false
            if (navController.currentDestination?.id != destino) {
                val opcoes = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.InicioFragment, false)
                    .build()
                navController.navigate(destino, null, opcoes)
            }
            true
        }

        binding.fab.setOnClickListener { navController.navigate(R.id.MensagensFragment) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val id = destination.id
            val principal = destinos.containsValue(id)
            val acesso = id == R.id.AberturaFragment || id == R.id.ApresentacaoFragment || id == R.id.LoginFragment || id == R.id.CadastroFragment || id == R.id.RecuperarSenhaFragment
            binding.bottomNavigation.visibility = if (principal) View.VISIBLE else View.GONE
            binding.toolbar.visibility = if (principal || acesso) View.GONE else View.VISIBLE
            if (principal || acesso || id == R.id.MensagensFragment || id == R.id.ConversaFragment) {
                binding.fab.hide()
            } else {
                binding.fab.show()
            }
            if (principal) {
                destinos.entries.firstOrNull { it.value == id }?.let { (tab, _) ->
                    binding.bottomNavigation.menu.findItem(tab).isChecked = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean {

        menuInflater.inflate(
            R.menu.menu_main,
            menu
        )

        atualizarTextoTema(menu)

        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {

        return when (item.itemId) {

            R.id.action_theme -> {

                trocarTema()

                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun trocarTema() {

        val modoAtual =
            AppCompatDelegate.getDefaultNightMode()

        if (modoAtual == AppCompatDelegate.MODE_NIGHT_YES) {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

        } else {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    private fun atualizarTextoTema(
        menu: Menu
    ) {

        val itemTema =
            menu.findItem(R.id.action_theme)

        val modoAtual =
            AppCompatDelegate.getDefaultNightMode()

        if (modoAtual == AppCompatDelegate.MODE_NIGHT_YES) {

            itemTema.title = "☀️ Tema claro"

        } else {

            itemTema.title = "🌙 Tema escuro"
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        val navController =
            findNavController(
                R.id.nav_host_fragment_content_main
            )

        return navController.navigateUp(
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}