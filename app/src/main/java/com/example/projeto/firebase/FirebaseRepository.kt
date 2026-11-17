package com.example.projeto.firebase

import com.example.projeto.data.Product
import com.example.projeto.data.Photo
import com.example.projeto.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import com.example.projeto.cloud.CloudinaryUploader

/** Firebase-backed operations. Firestore collections: usuarios, materiais. */
object FirebaseRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private fun uid(): String = auth.currentUser?.uid ?: error("Faça login para continuar")

    suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val id = result.user?.uid ?: error("Conta não criada")
        db.collection("usuarios").document(id).set(mapOf(
            "id" to id, "name" to name, "email" to email,
            "bio" to "", "locale" to "", "photoUrl" to "", "rating" to ""
        )).await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() = auth.signOut()

    suspend fun getProfile(): User {
        val id = uid()
        val doc = db.collection("usuarios").document(id).get().await()
        return User(id, doc.getString("name"), auth.currentUser?.email,
            doc.getString("photoUrl"), doc.getString("bio"),
            doc.getString("locale"), doc.getString("rating"))
    }

    private fun product(d: com.google.firebase.firestore.DocumentSnapshot): Product = Product(
        id = d.id, title = d.getString("title") ?: "",
        location = d.getString("location") ?: "",
        description = d.getString("description"), state = d.getString("state") ?: "",
        quantity = (d.getLong("quantity") ?: 0L).toInt(),
        category = d.getString("category") ?: "",
        authorId = d.getString("authorId") ?: "",
        photos = (d.get("photos") as? List<*>)?.mapNotNull { entry ->
            (entry as? Map<*, *>)?.let { Photo(it["id"].toString(), it["url"].toString()) }
        } ?: emptyList(),
        modalidade = d.getString("modalidade") ?: "Reaproveitamento",
        preco = d.getDouble("preco"),
        trocaPor = d.getString("trocaPor")
    )

    suspend fun getProducts(): List<Product> = db.collection("materiais").get().await().documents.map(::product)
    suspend fun getMyProducts(): List<Product> = db.collection("materiais")
        .whereEqualTo("authorId", uid()).get().await().documents.map(::product)

    private fun bytes(body: RequestBody): ByteArray {
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readByteArray()
    }
    private fun value(body: RequestBody?): String = body?.let { String(bytes(it), Charsets.UTF_8) } ?: ""

    suspend fun announceProduct(
        title: RequestBody, location: RequestBody, description: RequestBody?,
        state: RequestBody, quantity: RequestBody, category: RequestBody,
        images: List<MultipartBody.Part>,
        modalidade: String = "Doação", preco: Double? = null, trocaPor: String? = null
    ) {
        require(images.isNotEmpty()) { "Adicione pelo menos uma foto do produto." }
        val id = db.collection("materiais").document().id
        val photos = images.map { part ->
            val uploaded = CloudinaryUploader.upload(part)
            mapOf("id" to uploaded.publicId, "url" to uploaded.url)
        }
        db.collection("materiais").document(id).set(mapOf(
            "id" to id, "title" to value(title), "location" to value(location),
            "description" to value(description), "state" to value(state),
            "quantity" to (value(quantity).toIntOrNull() ?: 0),
            "category" to value(category), "authorId" to uid(),
            "modalidade" to modalidade,
            "preco" to (if (modalidade == "Venda") preco else null),
            "trocaPor" to (if (modalidade == "Troca") trocaPor else null),
            "photos" to photos, "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )).await()
    }

    suspend fun updateProfile(name: RequestBody?, bio: RequestBody?, locale: RequestBody?, photo: MultipartBody.Part?) {
        val fields = mutableMapOf<String, Any>("name" to value(name), "bio" to value(bio), "locale" to value(locale))
        if (photo != null) {
            fields["photoUrl"] = CloudinaryUploader.upload(photo).url
        }
        db.collection("usuarios").document(uid()).update(fields).await()
    }
}
