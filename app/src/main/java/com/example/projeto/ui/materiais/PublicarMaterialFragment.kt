package com.example.projeto.ui.materiais

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projeto.firebase.FirebaseRepository
import com.example.projeto.databinding.FragmentPublicarMaterialBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.projeto.R

class PublicarMaterialFragment : Fragment() {

    private var _binding: FragmentPublicarMaterialBinding? = null
    private val binding get() = _binding!!

    private var imagemSelecionada: Uri? = null

    // ==========================================
    // SELECIONAR FOTO
    // ==========================================

    private val selecionarFoto =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                imagemSelecionada = uri

                if (_binding != null) {
                    binding.imgMaterial.visibility = View.VISIBLE
                    binding.imgMaterial.setImageURI(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPublicarMaterialBinding.inflate(
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

        // ==========================================
        // CATEGORIAS
        // ==========================================

        val nomesCategorias = listOf(
            "Plástico", "Papel", "Vidro", "Metal", "Madeira", "Tecido",
            "Eletrônicos", "Móveis", "Roupas", "Materiais escolares", "Outros"
        )

        val valoresCategorias = listOf(
            "Plastico", "Papel", "Vidro", "Metal", "Madeira", "Tecido",
            "Eletronicos", "Moveis", "Roupas", "MateriaisEscolares", "Outros"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nomesCategorias
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerCategoria.adapter = adapter

        val modalidades = listOf("Doação", "Troca", "Venda")
        val modalidadesAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, modalidades
        )
        modalidadesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerModalidade.adapter = modalidadesAdapter
        binding.spinnerModalidade.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val modo = modalidades[position]
                binding.edtPrecoMaterial.visibility = if (modo == "Venda") View.VISIBLE else View.GONE
                binding.edtAceitaTroca.visibility = if (modo == "Troca") View.VISIBLE else View.GONE
            }
        }


        // ==========================================
        // SELECIONAR FOTO
        // ==========================================

        binding.btnSelecionarFoto.setOnClickListener {

            selecionarFoto.launch("image/*")
        }


        // ==========================================
        // PUBLICAR
        // ==========================================

        binding.btnPublicar.setOnClickListener {

            val material =
                binding.edtMaterial.text.toString().trim()

            val categoria =
                valoresCategorias[
                    binding.spinnerCategoria.selectedItemPosition
                ]

            val descricao =
                binding.edtDescricaoMaterial.text.toString().trim()

            val quantidade =
                binding.edtQuantidade.text.toString().trim()

            val estado =
                binding.edtEstado.text.toString().trim()

            val localizacao =
                binding.edtLocalizacaoMaterial.text.toString().trim()

            val modalidade = binding.spinnerModalidade.selectedItem.toString()
            val precoTexto = binding.edtPrecoMaterial.text.toString().trim()
            val preco = precoTexto.replace(",", ".").toDoubleOrNull()
            val trocaPor = binding.edtAceitaTroca.text.toString().trim()


            // ==========================================
            // VALIDAÇÕES
            // ==========================================

            if (material.isEmpty()) {

                binding.edtMaterial.error =
                    "Digite o nome do material"

                binding.edtMaterial.requestFocus()

                return@setOnClickListener
            }

            if (descricao.isEmpty()) {

                binding.edtDescricaoMaterial.error =
                    "Digite uma descrição"

                binding.edtDescricaoMaterial.requestFocus()

                return@setOnClickListener
            }

            if (quantidade.isEmpty()) {

                binding.edtQuantidade.error =
                    "Digite a quantidade"

                binding.edtQuantidade.requestFocus()

                return@setOnClickListener
            }

            if (estado.isEmpty()) {

                binding.edtEstado.error =
                    "Informe o estado do material"

                binding.edtEstado.requestFocus()

                return@setOnClickListener
            }

            if (localizacao.isEmpty()) {

                binding.edtLocalizacaoMaterial.error =
                    "Informe a localização"

                binding.edtLocalizacaoMaterial.requestFocus()

                return@setOnClickListener
            }

            if (imagemSelecionada == null) {

                Toast.makeText(
                    requireContext(),
                    "Selecione uma imagem",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            if (modalidade == "Venda" && (preco == null || preco <= 0.0)) {
                binding.edtPrecoMaterial.error = "Informe um preço válido"
                binding.edtPrecoMaterial.requestFocus()
                return@setOnClickListener
            }
            if (modalidade == "Troca" && trocaPor.isBlank()) {
                binding.edtAceitaTroca.error = "Informe o que aceita em troca"
                binding.edtAceitaTroca.requestFocus()
                return@setOnClickListener
            }

            // ==========================================
            // ENVIAR PARA API
            // ==========================================

            viewLifecycleOwner.lifecycleScope.launch {

                try {

                    binding.btnPublicar.isEnabled = false


                    // ==========================================
                    // REQUEST BODIES
                    // ==========================================

                    val titleBody =
                        material.toRequestBody(
                            "text/plain".toMediaType()
                        )

                    val locationBody =
                        localizacao.toRequestBody(
                            "text/plain".toMediaType()
                        )

                    val descriptionBody =
                        descricao.toRequestBody(
                            "text/plain".toMediaType()
                        )

                    val stateBody =
                        estado.toRequestBody(
                            "text/plain".toMediaType()
                        )

                    val quantityBody =
                        quantidade.toRequestBody(
                            "text/plain".toMediaType()
                        )

                    val categoryBody =
                        categoria.toRequestBody(
                            "text/plain".toMediaType()
                        )


                    // ==========================================
                    // IMAGEM
                    // ==========================================

                    val imagemPart =
                        uriParaMultipart(
                            imagemSelecionada!!
                        )


                    // ==========================================
                    // REQUISIÇÃO
                    // ==========================================

                    FirebaseRepository.announceProduct(
                            title = titleBody,
                            location = locationBody,
                            description = descriptionBody,
                            state = stateBody,
                            quantity = quantityBody,
                            category = categoryBody,
                            images = listOf(imagemPart),
                            modalidade = modalidade,
                            preco = preco,
                            trocaPor = trocaPor.ifBlank { null }
                        )


                    // ==========================================
                    // RESPOSTA
                    // ==========================================

                    Toast.makeText(requireContext(), "Anúncio publicado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_PublicarMaterialFragment_to_MeusAnunciosFragment)

                } catch (e: Exception) {

                    binding.btnPublicar.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Erro: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ==========================================
    // CONVERTER URI → MULTIPART
    // ==========================================

    private fun uriParaMultipart(
        uri: Uri
    ): MultipartBody.Part {

        val inputStream =
            requireContext()
                .contentResolver
                .openInputStream(uri)
                ?: throw Exception(
                    "Não foi possível abrir a imagem"
                )

        val arquivoTemporario =
            File.createTempFile(
                "imagem_",
                ".jpg",
                requireContext().cacheDir
            )

        inputStream.use { input ->

            arquivoTemporario.outputStream().use { output ->

                input.copyTo(output)
            }
        }

        val requestBody =
            arquivoTemporario.asRequestBody(
                "image/*".toMediaType()
            )

        return MultipartBody.Part.createFormData(
            "Images",
            arquivoTemporario.name,
            requestBody
        )
    }


    // ==========================================
    // LIMPAR BINDING
    // ==========================================

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}