package hr.unidu.kz.primjerweb

import kotlinx.serialization.Serializable

@Serializable
data class Praznik(
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val fixed: Boolean,
    val global: Boolean,
    val counties: List<String>? = null, // Može biti null u JSON-u
    val launchYear: Int? = null,        // Može biti null u JSON-u
    val types: List<String>
)