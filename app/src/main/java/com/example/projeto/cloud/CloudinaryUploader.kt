package com.example.projeto.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Envia imagens ao preset unsigned; nunca coloque API Secret no aplicativo. */
object CloudinaryUploader {
    // Confirme o Cloud Name em Cloudinary > Dashboard antes de publicar o app.
    private const val CLOUD_NAME = "ew3kij8q"
    private const val UPLOAD_PRESET = "rematch_produtos"
    private const val MAX_BYTES = 5 * 1024 * 1024L

    data class UploadedImage(val publicId: String, val url: String)

    private val client = OkHttpClient.Builder()
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun upload(image: MultipartBody.Part): UploadedImage = withContext(Dispatchers.IO) {
        val original = image.body
        val mime = original.contentType()?.toString()?.lowercase() ?: ""
        // O formulário antigo usa image/*; aceitamos isso apenas se os bytes forem uma imagem reconhecida.
        val buffer = Buffer()
        original.writeTo(buffer)
        val bytes = buffer.readByteArray()
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) {
            throw IOException("A imagem deve ter no máximo 5 MB")
        }
        val isJpeg = bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        val isPng = bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(137.toByte(),80,78,71,13,10,26,10))
        val isWebp = bytes.size >= 12 && String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF" && String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP"
        val actualMime = when {
            isJpeg -> "image/jpeg"
            isPng -> "image/png"
            isWebp -> "image/webp"
            else -> throw IOException("Use uma imagem JPG, PNG ou WebP")
        }
        val extension = when (actualMime) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("file", "rematch.$extension", bytes.toRequestBody(actualMime.toMediaType()))
            .build()
        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching { JSONObject(text).optJSONObject("error")?.optString("message") }.getOrNull()
                throw IOException("Falha ao enviar imagem (${response.code}): ${detail ?: "verifique o preset Cloudinary"}")
            }
            val json = JSONObject(text)
            val url = json.optString("secure_url")
            val id = json.optString("public_id")
            if (!url.startsWith("https://") || id.isBlank()) throw IOException("Resposta inválida do Cloudinary")
            UploadedImage(id, url)
        }
    }
}
