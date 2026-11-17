package com.example.projeto.ui.perfil

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projeto.firebase.FirebaseRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import com.bumptech.glide.Glide
import com.example.projeto.R

class EditarPerfilFragment : Fragment() {

    private var fotoSelecionada: Uri? = null

    // =========================
    // SELECIONAR FOTO
    // =========================

    private val selecionarFoto =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                fotoSelecionada = uri

                view?.findViewById<ImageView>(
                    R.id.imgFotoPerfil
                )?.setImageURI(uri)
            }
        }

    // =========================
    // CRIAR VIEW
    // =========================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_editar_perfil,
            container,
            false
        )
    }

    // =========================
    // VIEW CRIADA
    // =========================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        val imgFotoPerfil =
            view.findViewById<ImageView>(R.id.imgFotoPerfil)

        val btnSelecionarFotoPerfil =
            view.findViewById<Button>(R.id.btnSelecionarFotoPerfil)

        val btnSalvarPerfil =
            view.findViewById<Button>(R.id.btnSalvarPerfil)

        val btnVoltarPerfil =
            view.findViewById<Button>(R.id.btnVoltarPerfil)

        val edtNomePerfil =
            view.findViewById<EditText>(R.id.edtNomePerfil)

        val edtLocalizacaoPerfil =
            view.findViewById<EditText>(R.id.edtLocalizacaoPerfil)

        val edtDescricaoPerfil =
            view.findViewById<EditText>(R.id.edtDescricaoPerfil)


        // =========================
        // CARREGAR PERFIL
        // =========================

        carregarPerfil(
            edtNomePerfil,
            edtLocalizacaoPerfil,
            edtDescricaoPerfil,
            imgFotoPerfil
        )


        // =========================
        // SELECIONAR FOTO
        // =========================

        btnSelecionarFotoPerfil.setOnClickListener {

            selecionarFoto.launch("image/*")
        }


        // =========================
        // SALVAR
        // =========================

        btnSalvarPerfil.setOnClickListener {

            atualizarPerfil(
                edtNomePerfil,
                edtLocalizacaoPerfil,
                edtDescricaoPerfil
            )
        }


        // =========================
        // VOLTAR
        // =========================

        btnVoltarPerfil.setOnClickListener {

            findNavController().navigate(
                R.id.action_EditarPerfilFragment_to_PerfilFragment
            )
        }
    }


    // =========================
    // GET /users/profile
    // =========================

    private fun carregarPerfil(
        edtNome: EditText,
        edtLocalizacao: EditText,
        edtDescricao: EditText,
        imgFoto: ImageView
    ) {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val user = FirebaseRepository.getProfile()
                edtNome.setText(user.name ?: "")
                edtLocalizacao.setText(user.locale ?: "")
                edtDescricao.setText(user.bio ?: "")
                if (!user.photoUrl.isNullOrBlank()) {
                    Glide.with(this@EditarPerfilFragment).load(user.photoUrl).into(imgFoto)
                }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // =========================
    // PATCH /users/update
    // =========================

    private fun atualizarPerfil(
        edtNome: EditText,
        edtLocalizacao: EditText,
        edtDescricao: EditText
    ) {

        val nome =
            edtNome.text.toString()

        val localizacao =
            edtLocalizacao.text.toString()

        val descricao =
            edtDescricao.text.toString()


        // =========================
        // TRANSFORMAR TEXTOS
        // =========================

        val nomeBody =
            nome.toRequestBody(
                "text/plain".toMediaType()
            )

        val localizacaoBody =
            localizacao.toRequestBody(
                "text/plain".toMediaType()
            )

        val descricaoBody =
            descricao.toRequestBody(
                "text/plain".toMediaType()
            )


        // =========================
        // FOTO
        // =========================

        val fotoPart =
            fotoSelecionada?.let { uri ->

                criarMultipartFoto(uri)
            }


        // =========================
        // ENVIAR
        // =========================

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                FirebaseRepository.updateProfile(
                        name = nomeBody,
                        bio = descricaoBody,
                        locale = localizacaoBody,
                        photo = fotoPart
                    )

                Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_EditarPerfilFragment_to_PerfilFragment)

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // =========================
    // TRANSFORMAR URI EM MULTIPART
    // =========================

    private fun criarMultipartFoto(
        uri: Uri
    ): MultipartBody.Part? {

        val resolver =
            requireContext().contentResolver

        val inputStream =
            resolver.openInputStream(uri)
                ?: return null

        val bytes =
            inputStream.readBytes()

        inputStream.close()

        val mimeType =
            resolver.getType(uri)
                ?: "image/jpeg"

        val requestBody =
            bytes.toRequestBody(
                mimeType.toMediaType()
            )

        return MultipartBody.Part.createFormData(
            "PhotoUrl",
            "profile.jpg",
            requestBody
        )
    }
}